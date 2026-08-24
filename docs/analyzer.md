
---

# Analyzer — Analizador Estático de Código (Linter)

El analyzer (Static code analyzer) recibe nodos del AST y los recorre para notificar el incumplimiento de reglas, ya sea para imponer convenciones de formato o detectar malas prácticas, indicando la posición exacta del problema. Vive en el módulo `analyzer`, paquete `org.printscript.analyzer`.

---

## Archivos y su responsabilidad

### `StaticAnalyzer.java` — interfaz pública

Define los métodos para analizar sentencias individualmente (para no sobrecargar la memoria) y luego recolectar los resultados:

```java
public interface StaticAnalyzer {
    void analyze(Statement statement);
    List<Violation> getViolations();
}

```

### `StaticAnalyzerImpl.java` — implementación

Recibe la configuración por constructor, instancia el visitor y actúa como fachada (wrapper) para exponer los métodos de la interfaz:

```java
public class StaticAnalyzerImpl implements StaticAnalyzer {
    public StaticAnalyzerImpl(AnalyzerConfig config) { ... }
    public void analyze(Statement statement) { statement.accept(visitor); }
    public List<Violation> getViolations() { return visitor.getFinalViolations(); }
}

```

### `AnalyzerVisitor.java` — el visitor

Implementa `ASTVisitor<Void>`. A diferencia del formatter, no devuelve `String`. Su trabajo es recorrer el árbol evaluando cada nodo contra las reglas activas. Si detecta una infracción, crea un objeto `Violation` y lo guarda en una lista interna. Utiliza la clase `Environment` de `common` para llevar un registro del estado y uso de las variables.

### `AnalyzerConfig.java` — configuración

Objeto que contiene las reglas del linter. Se carga desde un archivo JSON (usando Jackson) o se inicializa con valores por defecto.

### `Violation.java` y `Severity.java` — modelo de errores

Representan el problema encontrado. Contienen el mensaje descriptivo, la severidad (`WARNING` o `ERROR`), y un objeto `Position` que indica la columna y fila de inicio y fin del problema.

---

## Reglas configurables

| Regla | Tipo | Default | Efecto |
| --- | --- | --- | --- |
| `activeNamingConvention` | boolean | `true` | Activa la validación de nombres de identificadores. |
| `namingConventionFormat` | Enum | `camelCase` | Define si el formato debe ser `camelCase` o `snake_case`.

|
| `activeComplexPrintln` | boolean | `true` | Restringe el uso de `println` a solo identificadores o literales, prohibiendo expresiones complejas.

|
| `activeUnusedVariables` | boolean | `true` | Detecta variables que fueron declaradas pero nunca se leyeron. |

### Ejemplo de JSON de configuración (`linter_rules.json`)

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
// 1. Cargar reglas desde JSON (o usar defaults si falla)
AnalyzerConfig config = AnalyzerConfig.fromJson(new FileReader("linter_rules.json"));

// 2. Instanciar el analizador
StaticAnalyzer staticAnalyzer = new StaticAnalyzerImpl(config);

// 3. Iterar sobre el programa (Statement por Statement)
while (statementIterator.hasNext()) {
    Statement stmt = statementIterator.next();
    staticAnalyzer.analyze(stmt);
}

// 4. Obtener y mostrar resultados
List<Violation> violations = staticAnalyzer.getViolations();
for (Violation v : violations) {
    System.out.println(v.toString());
}

```

---

## Ejemplo de entrada → salida

### Input (código con malas prácticas)

```typescript
let MiVariable: number = 5;
println(MiVariable + 1);

```

### Output (Violaciones reportadas)

```text
[WARNING] Variable 'MiVariable' should be in camelCase. at 1:5-1:15
[WARNING] Complex expression in 'println'. Consider extracting to an intermediate variable. at 2:9-2:23
[WARNING] Variable 'MiVariable' is declared but never used. at 1:5-1:15

```

Notar:

* `MiVariable` rompe la regla de `camelCase`.
* El `println` contiene una expresión binaria (`+`), lo cual rompe la regla de expresiones complejas.


* Como `MiVariable` solo se reasignó/operó pero la regla detecta usos complejos, dependiendo de la implementación de "uso", puede marcarse como no leída de forma aislada.

---

## Diseño

```text
AnalyzerConfig (configuración)
       │
       ▼
StaticAnalyzerImpl (implements StaticAnalyzer)
       │
       ▼
AnalyzerVisitor (implements ASTVisitor<Void>)
       │   ├── Environment (guarda LinterMetadata)
       │   └── List<Violation> (acumula warnings)
       │
       ├── visitVarDeclaration  → Verifica naming convention & registra variable
       ├── visitCallExpr        → Verifica argumentos simples en println
       └── visitIdentifier      → Marca la variable como "usada" en el Environment

```

### Principios aplicados

* **Visitor pattern**: Permite evaluar reglas complejas sobre el AST sin contaminar las clases del modelo de datos `Node` / `Statement`.
* **Separation of Concerns**: Utiliza su propia instancia de `Environment` exclusivo para análisis estático, sin mezclarse con la memoria del intérprete ni con la tabla de tipos del analizador semántico.
* **Stateful Analysis**: A diferencia de otras herramientas, el linter mantiene el estado (qué variables se declararon y usaron) a lo largo de toda la iteración del archivo para procesar reglas globales como variables sin usar.

---

## Cómo se conecta con el resto

```text
Lexer (Iterator<Token>)
  │
  ▼
Parser (Iterator<Statement>)
  │
  ▼
CLI / Main (Controla el flujo de memoria)
  │
  ├─► SemanticAnalyzer.analyze(stmt) (Falla rápido si hay error de tipos)
  │
  └─► StaticAnalyzer.analyze(stmt) (Acumula warnings pasivamente)
        │
        ▼
List<Violation> (Reporte final al usuario)

```

El analizador procesa la información fluyendo de a partes (sentencia por sentencia) para soportar mecanismos que permitan manejar fuentes extensas sin consumir toda la memoria disponible, cumpliendo con la restricción técnica del diseño del CLI.