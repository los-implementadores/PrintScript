# Analyzer — Analizador Estático de Código (Linter)

El analyzer (static code analyzer) recorre el AST y reporta las reglas que no se cumplen, ya sea
para imponer convenciones o para detectar malas prácticas, con la posición exacta del problema.
Vive en el módulo `analyzer`, que depende solo de `ast` y de `rules`. No sabe de versiones del
lenguaje.

---

## Paquetes y archivos

| Paquete | Archivo | Responsabilidad |
| --- | --- | --- |
| `org.printscript.analyzer` | `StaticAnalyzer` | Interfaz pública: `analyze(Statement)` y `getViolations()` |
| | `StaticAnalyzerImpl` | Recorre cada sentencia con `Node.children()` y le avisa a cada regla los nodos que visita. Recibe una `RuleConfig` o una fábrica de reglas (`Supplier<List<LintRule>>`) |
| `org.printscript.analyzer.rule` | `LintRule` | Interfaz de una regla: `onEnter`, `onExit` y `onFinish` |
| | `LintRules` | **El único lugar donde se registran las reglas**, con su clave de configuración |
| | `NamingConventionRule`, `SimpleArgumentRule`, `UnusedVariableRule` | Una clase por regla |
| | `NamingConvention` | Las convenciones de nombres (camelCase y snake_case) |
| `org.printscript.analyzer.violation` | `Violation`, `Severity` | Lo que se reporta: mensaje, severidad (`WARNING` o `ERROR`) y `Position` |

La lectura del JSON y el mecanismo "clave → regla" (`RuleConfig`, `RuleRegistry`) están en el
módulo `rules`, que comparte con el formatter (ADR-026).

---

## Reglas configurables

Todas las reglas están **apagadas por defecto**. Cada una se puede activar con su clave propia o
con la clave que usa el TCK.

| Regla | Clave propia | Clave del TCK | Qué reporta |
| --- | --- | --- | --- |
| Formato de nombres | `activeNamingConvention` + `namingConventionFormat` (`"camelCase"` o `"snake_case"`; camelCase si no está) | `identifier_format` (`"camel case"` o `"snake case"`) | Variables cuyo nombre no sigue la convención |
| Argumento simple en `println` | `activeComplexPrintln` | `mandatory-variable-or-literal-in-println` | Un `println` cuyo argumento no es un literal ni una variable |
| Argumento simple en `readInput` | `activeComplexReadInput` | `mandatory-variable-or-literal-in-readInput` | Lo mismo para `readInput` |
| Variables sin usar | `activeUnusedVariables` | — | Variables declaradas que nunca se leen ni se reasignan |

Las claves desconocidas se ignoran. Un JSON mal formado, un valor del tipo equivocado
(`"activeUnusedVariables": "sí"`) o una convención que no existe (`"kebab case"`) son un error: la
CLI lo informa y termina con exit code 1, en vez de analizar sin reglas.

### Ejemplo de configuración (`linter_rules.json`)

```json
{
  "activeNamingConvention": true,
  "namingConventionFormat": "camelCase",
  "activeComplexPrintln": true,
  "activeUnusedVariables": true
}
```

---

## Uso típico

Lo normal es usarlo a través del runner, que además hace el chequeo semántico:

```java
List<Violation> violations =
    LanguageServices.printScript()
        .analysis()
        .analyze(reader, LanguageVersion.V1_1, RuleConfig.fromJson(configReader));
```

Directo, sentencia por sentencia (no hace falta tener todo el programa en memoria):

```java
StaticAnalyzer analyzer = new StaticAnalyzerImpl(RuleConfig.fromJson(configReader));
while (statements.hasNext()) {
    analyzer.analyze(statements.next());
}
for (Violation v : analyzer.getViolations()) {   // se puede pedir más de una vez
    System.out.println(v);
}
```

---

## Ejemplo de entrada y salida

Con la configuración `{"identifier_format": "camel case", "mandatory-variable-or-literal-in-println": true, "activeUnusedVariables": true}`:

```typescript
let MiVariable: number = 5;
let sinUso: number = 2;
println(MiVariable + 1);
```

```text
[WARNING] Variable 'MiVariable' should be in camelCase. at 1:1-1:27
[WARNING] Complex expression in 'println'. Consider extracting to an intermediate variable. at 3:1-3:23
[WARNING] Variable 'sinUso' is declared but never used. at 2:1-2:23
```

- `MiVariable` empieza con mayúscula, así que no es camelCase. La posición es la de la declaración.
- El argumento de `println` es una suma, no un literal ni una variable.
- `sinUso` nunca se lee. Las variables sin usar se reportan al final, porque recién ahí se sabe
  que nadie las usó. `MiVariable` no aparece: se lee en el `println`.

---

## Diseño

```text
RuleConfig ──LintRules.create(config)──► List<LintRule>
                                               │
StaticAnalyzerImpl                             │
  recorre cada sentencia con children() ───────┤ onEnter(nodo) / onExit(nodo)
  y acumula lo que reportan las reglas         │ onFinish() al pedir el reporte
```

El recorrido es genérico: `StaticAnalyzerImpl` baja por `Node.children()` y le avisa a cada regla
cada nodo, al entrar y al salir. No conoce ningún tipo de nodo ni ninguna regla. Cada regla mira
los nodos que le interesan (`if (node instanceof CallExpression call)`) e ignora el resto:

| Regla | Mira | Estado propio |
| --- | --- | --- |
| `NamingConventionRule` | declaraciones | ninguno |
| `SimpleArgumentRule` | llamadas a la función que controla | ninguno |
| `UnusedVariableRule` | declaraciones, asignaciones, lecturas y bloques; reporta en `onFinish` | qué variables se declararon, en qué scope y cuáles se usaron |

### Por qué no un visitor

Con un `ASTVisitor`, cada nodo nuevo del lenguaje obliga a agregar su `visitX` en el linter aunque
ninguna regla lo mire (el *expression problem*, ADR-009). En el linter lo que más cambia son los
nodos, no las operaciones, así que con `children()` un nodo nuevo no lo toca: solo tiene que decir
cuáles son sus hijos. Si es un cuerpo entre llaves, reusa `Block` y `UnusedVariableRule` ya maneja
su scope (ADR-027).

### Cómo agregar una regla

1. Escribir una clase que implemente `LintRule` en `org.printscript.analyzer.rule`.
2. Registrarla con su clave en `LintRules`:

```java
.rule("noShortNames", "min-identifier-length").whenOn(ShortNameRule::new)
```

Con eso ya se activa desde el JSON: ni el recorrido ni la lectura de la configuración cambian. Una
regla que no viene del JSON se le puede pasar directo con
`new StaticAnalyzerImpl(() -> List.of(new MiRegla()))`. Es una fábrica y no una lista porque las
reglas guardan estado (por ejemplo, qué variables se usaron): cada análisis tiene que arrancar con
reglas nuevas.

### Principios aplicados

- **Open/Closed:** agregar una regla o un tipo de nodo no modifica el recorrido.
- **Single Responsibility:** `StaticAnalyzerImpl` recorre, cada regla decide, `LintRules` registra
  y `RuleConfig` lee la configuración.
- **Estado aislado:** cada regla guarda su propio estado; el linter no comparte nada con la
  memoria del intérprete ni con la tabla de tipos del semántico.

---

## Cómo se conecta con el resto

```text
Lexer (Iterator<Token>)
  │
  ▼
Parser (Iterator<Statement>, gramática de la versión)
  │
  ▼
AnalysisService (runner)
  │
  ├─► SemanticAnalyzer.analyze(stmt)   (falla en el primer error de tipos)
  │
  └─► StaticAnalyzer.analyze(stmt)     (acumula violaciones)
        │
        ▼
List<Violation> (reporte final)
```

El analizador procesa el programa sentencia por sentencia, así que no necesita tenerlo entero en
memoria.
