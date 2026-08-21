# Formatter — Formateador de código

El formatter recibe un AST (un `Program`) y produce el código fuente reformateado
según reglas configurables. Vive en el módulo `formatter`, paquete
`org.printscript.formatter`.

---

## Archivos y su responsabilidad

### `Formatter.java` — interfaz pública

Define un único método:

```java
public interface Formatter {
    String format(Program program);
}
```

### `FormatterImpl.java` — implementación

Recibe las reglas por constructor y delega en el visitor:

```java
public class FormatterImpl implements Formatter {
    public FormatterImpl(FormattingRules rules) { ... }
    public String format(Program program) {
        return program.accept(new FormatterVisitor(rules));
    }
}
```

### `FormatterVisitor.java` — el visitor

Implementa `ASTVisitor<String>`. Cada método `visit*` retorna el fragmento
de código formateado para ese nodo. El resultado se compone concatenando
los fragmentos de arriba a abajo.

### `FormattingRules.java` — configuración

Objeto que contiene las reglas de formateo. Se puede crear con valores por
defecto (constructor vacío) o cargar desde un JSON:

```java
FormattingRules rules = FormattingRules.fromJson(reader);
```

---

## Reglas configurables

| Regla | Tipo | Default | Efecto |
|-------|------|---------|--------|
| `spaceBeforeColon` | boolean | `false` | `let x: number` vs `let x : number` |
| `spaceAfterColon` | boolean | `true` | `let x: number` vs `let x:number` |
| `spaceAroundAssign` | boolean | `true` | `x = 5` vs `x=5` |
| `spaceAroundOperators` | boolean | `true` | `a + b` vs `a+b` |
| `newlineBeforePrintln` | int | `1` | Líneas en blanco antes de llamadas a función |

### Ejemplo de JSON de configuración

```json
{
  "spaceBeforeColon": false,
  "spaceAfterColon": true,
  "spaceAroundAssign": true,
  "spaceAroundOperators": true,
  "newlineBeforePrintln": 1
}
```

---

## Uso típico

```java
// 1. Parsear el código fuente
Lexer lexer = new LexerImpl(new StringReader(source), matchers);
Parser parser = new ParserImpl(lexer);
Program program = new LazyProgram(parser, startPos);

// 2. Cargar reglas desde JSON (o usar defaults)
FormattingRules rules = FormattingRules.fromJson(new FileReader("format-rules.json"));

// 3. Formatear
Formatter formatter = new FormatterImpl(rules);
String formatted = formatter.format(program);
```

---

## Ejemplo de entrada → salida

### Input (desformateado)

```
let name:string="Joe";
let lastName:string ="Doe";
println(name +" "+ lastName);
```

### Output (con reglas por defecto)

```
let name: string = "Joe";
let lastName: string = "Doe";

println(name + " " + lastName);
```

Notar:
- Se agregaron espacios alrededor de `=` y `:` según las reglas
- Se agregaron espacios alrededor de `+`
- Se insertó una línea en blanco antes del `println` (`newlineBeforePrintln = 1`)

---

## Diseño

```
FormattingRules (configuración)
       │
       ▼
FormatterImpl (implements Formatter)
       │
       ▼
FormatterVisitor (implements ASTVisitor<String>)
       │
       ├── visitVarDeclaration  → "let x: number = 5;"
       ├── visitAssignment      → "x = 10;"
       ├── visitExpressionStmt  → "println(x);"
       ├── visitBinaryExpr      → "a + b"
       ├── visitCallExpr        → "println(x)"
       ├── visitNumberLiteral   → "42"
       ├── visitStringLiteral   → "\"Joe\""
       └── visitIdentifier      → "name"
```

### Principios aplicados

- **Visitor pattern** (ADR-002): el formatter recorre el AST sin modificar los nodos.
- **Open/Closed**: para cambiar el formato, se pasa un `FormattingRules` distinto
  — no se modifica el visitor.
- **Single Responsibility**: `FormattingRules` solo sabe de configuración,
  `FormatterVisitor` solo sabe recorrer el árbol, `FormatterImpl` solo orquesta.

---

## Cómo se conecta con el resto

```
Lexer (Iterator<Token>)
  │
  ▼
Parser (Iterator<Statement>)
  │
  ▼
LazyProgram (implements Program)
  │
  ▼
FormatterImpl.format(program)
  │
  ▼
String (código fuente formateado)
```

El formatter no depende del lexer ni del parser en runtime — solo de `common`
(los nodos del AST). Las dependencias de `lexer` y `parser` en el `build.gradle.kts`
son solo para los tests (parsear código fuente de ejemplo).
