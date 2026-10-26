# Analyzer — Analizador Estático de Código (Linter)

El analyzer (static code analyzer) recorre el AST y reporta las reglas que no se cumplen, ya sea
para imponer convenciones o para detectar malas prácticas, con la posición exacta del problema.
Vive en el módulo `analyzer`, que depende solo de `common` y de Jackson.

---

## Paquetes y archivos

| Paquete | Archivo | Responsabilidad |
| --- | --- | --- |
| `org.printscript.analyzer` | `StaticAnalyzer` | Interfaz pública: `analyze(Statement)` y `getViolations()` |
| | `StaticAnalyzerImpl` | Fachada. Recibe una `AnalyzerConfig` o una fábrica de reglas (`Supplier<List<LintRule>>`) |
| | `AnalyzerVisitor` | Recorre el AST (`ASTVisitor<Void>`) y le avisa a cada regla los nodos que visita |
| `org.printscript.analyzer.rule` | `LintRule` | Interfaz de una regla |
| | `LintRules` | Único lugar donde se registran las reglas configurables |
| | `NamingConventionRule`, `SimpleArgumentRule`, `UnusedVariableRule` | Una clase por regla |
| `org.printscript.analyzer.config` | `AnalyzerConfig`, `NamingConvention` | Configuración, leída de JSON con Jackson |
| `org.printscript.analyzer.violation` | `Violation`, `Severity` | Lo que se reporta: mensaje, severidad (`WARNING` o `ERROR`) y `Position` |

Hasta la versión 1.1.0 la configuración y las violaciones vivían en `common`; ver ADR-014.

---

## Reglas configurables

Todas las reglas están **apagadas por defecto**. Cada una se puede activar con su clave propia o
con la clave que usa el TCK.

| Regla | Clave propia | Clave del TCK | Qué reporta |
| --- | --- | --- | --- |
| Formato de nombres | `activeNamingConvention` + `namingConventionFormat` (`"camelCase"` o `"snake_case"`) | `identifier_format` (`"camel case"` o `"snake case"`) | Variables cuyo nombre no sigue la convención |
| Argumento simple en `println` | `activeComplexPrintln` | `mandatory-variable-or-literal-in-println` | Un `println` cuyo argumento no es un literal ni una variable |
| Argumento simple en `readInput` (1.1) | `activeComplexReadInput` | `mandatory-variable-or-literal-in-readInput` | Lo mismo para `readInput` |
| Variables sin usar | `activeUnusedVariables` | — | Variables declaradas que nunca se leen ni se reasignan |

Las claves desconocidas se ignoran.

> **Problema conocido:** si el JSON no se puede leer, `AnalyzerConfig.fromJson` avisa con una
> línea en `stderr` y sigue con la configuración por defecto, o sea, con **todas las reglas
> apagadas**. Basta un typo (`"identifier_format": "kebab case"`) para que el CLI diga
> «Analyzing successful» con exit 0 sin haber aplicado ninguna regla. Además, queda al revés que
> el formatter, que ante un JSON roto falla. Lo correcto es fallar; el PR #57 cambia `fromJson`
> para que lance una excepción.

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

```java
// 1. Cargar las reglas desde JSON
AnalyzerConfig config = AnalyzerConfig.fromJson(new FileReader("linter_rules.json"));

// 2. Instanciar el analizador
StaticAnalyzer staticAnalyzer = new StaticAnalyzerImpl(config);

// 3. Analizar sentencia por sentencia (no hace falta tener todo el programa en memoria)
while (statementIterator.hasNext()) {
    staticAnalyzer.analyze(statementIterator.next());
}

// 4. Pedir el reporte (se puede pedir más de una vez)
for (Violation v : staticAnalyzer.getViolations()) {
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
AnalyzerConfig ──LintRules.from(config)──► List<LintRule>
                                                 │
StaticAnalyzerImpl (fachada)                     │
       │                                         │
       ▼                                         ▼
AnalyzerVisitor (ASTVisitor<Void>) ── avisa a cada regla ──► onDeclaration / onAssignment
       │                                                     onCall / onIdentifier
       │                                                     onBlockStart / onBlockEnd / onFinish
       └── List<Violation> (acumula lo que reportan las reglas)
```

El visitor solo recorre el árbol; no sabe qué reglas existen. Cada regla implementa `LintRule` y
sobreescribe solo los nodos que le interesan:

| Regla | Mira | Estado propio |
| --- | --- | --- |
| `NamingConventionRule` | declaraciones | ninguno |
| `SimpleArgumentRule` | llamadas a la función que controla | ninguno |
| `UnusedVariableRule` | declaraciones, asignaciones, lecturas y bloques; reporta en `onFinish` | qué variables se declararon, en qué scope y cuáles se usaron |

### Cómo agregar una regla

1. Escribir una clase que implemente `LintRule` en `org.printscript.analyzer.rule`.
2. Si se activa desde el JSON, agregar su clave en `AnalyzerConfig`.
3. Registrarla en `LintRules.from`.

El visitor no se toca. Una regla que no viene del JSON se le puede pasar directo con
`new StaticAnalyzerImpl(() -> List.of(new MiRegla()))`. Es una fábrica y no una lista porque las
reglas guardan estado (por ejemplo, qué variables se usaron): cada análisis tiene que arrancar con
reglas nuevas.

### Principios aplicados

- **Open/Closed:** agregar una regla no modifica el recorrido. Es el mismo criterio que
  `TokenMatcher` en el lexer y los parselets en el parser (ver ADR-015).
- **Visitor:** las reglas se evalúan sobre el AST sin agregar lógica a los nodos.
- **Estado aislado:** cada regla guarda su propio estado; el linter no comparte nada con la
  memoria del intérprete ni con la tabla de tipos del semántico.

---

## Cómo se conecta con el resto

```text
Lexer (Iterator<Token>)
  │
  ▼
Parser (Iterator<Statement>)
  │
  ▼
CLI / Main (modo analyzing)
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
