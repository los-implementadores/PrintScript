# Parser — Analizador Sintáctico

El parser consume los tokens producidos por el `Lexer` y produce sentencias del
AST de a una. Vive en el módulo `parser`, paquete `org.printscript.parser`.

---

## Archivos y su responsabilidad

### `Parser.java` — interfaz pública

Extiende `Iterator<Statement>`. El parser no devuelve un `Program` completo —
produce una sentencia por llamada a `next()`, sin cargar el árbol entero en memoria.

```java
public interface Parser extends Iterator<Statement> { }
```

### `ParseException.java` — error sintáctico

Lanzada en cuanto se detecta el primer error (fail-fast).
Incluye la `Position` exacta del token problemático.

### `ParserImpl.java` — implementación

Combina dos técnicas:

- **Recursivo descendente** para sentencias
- **Pratt parsing** (top-down operator precedence) para expresiones

---

## Uso típico

El parser se envuelve en un `LazyProgram` para que los módulos consumidores
reciban siempre la misma interfaz `Program`, sin saber que por abajo es lazy:

```java
Lexer lexer = new LexerImpl(new StringReader(source));
Parser parser = new ParserImpl(lexer);
Program program = new LazyProgram(parser, startPos);
```

A partir de ahí cada módulo elige cómo consumirlo:

```java
// Interpreter — procesa de a uno, bajo consumo de memoria:
Iterator<Statement> it = program.stream();
while (it.hasNext()) {
    it.next().accept(interpreterVisitor);
}

// Analyzer — necesita el árbol completo:
for (Statement stmt : program.toList()) {
    stmt.accept(analyzerVisitor);
}
```

Ningún módulo sabe si el `Program` es lazy o eager. La diferencia es
transparente para el caller.

---

## Gramática soportada (PrintScript 1.0)

```
program             → statement* EOF

statement           → varDeclaration
                    | assignment
                    | expressionStatement

varDeclaration      → "let" IDENTIFIER ":" typeName "=" expression ";"
assignment          → IDENTIFIER "=" expression ";"
expressionStatement → expression ";"

typeName            → "number" | "string"

expression          → pratt(0)

primary             → NUMBER_LITERAL
                    | STRING_LITERAL
                    | IDENTIFIER
                    | IDENTIFIER "(" arguments ")"
                    | "(" expression ")"

arguments           → expression?
```

---

## Cómo funciona el Pratt parser

A cada operador se le asigna un **binding power** (peso). La función central es:

```
parseExpression(minPower):
    left = parsePrimary()
    loop:
        si el operador actual tiene peso ≤ minPower → parar
        consumir operador
        right = parseExpression(peso_del_operador)
        left = BinaryExpression(left, op, right)
    return left
```

### Tabla de binding powers

| Operador | Peso | Asociatividad |
|----------|------|---------------|
| `+`      | 10   | izquierda     |
| `-`      | 10   | izquierda     |
| `*`      | 20   | izquierda     |
| `/`      | 20   | izquierda     |

### Ejemplo: `2 + 3 * 4`

```
parseExpression(0)
  left = 2
  op = +  (peso=10 > 0 ✅)
  right = parseExpression(10)
            left = 3
            op = *  (peso=20 > 10 ✅)
            right = parseExpression(20)
                      left = 4
                      op = EOF  (peso=0 > 20 ❌) → para
                      return 4
            left = BinaryExpr(3 * 4)
            op = EOF  (peso=0 > 10 ❌) → para
            return BinaryExpr(3 * 4)
  left = BinaryExpr(2 + BinaryExpr(3 * 4))   ✅
```

### Asociatividad izquierda

`1 + 2 + 3` → `(1 + 2) + 3`. La llamada recursiva recibe el mismo peso del
operador, por lo que el mismo operador a la derecha no entra — necesita
estrictamente mayor.

---

## Lookahead

El parser mantiene un campo `current` con el token que está por consumir.
No se modifica la interfaz `Lexer`.

El único caso que requiere anticipación de un token es distinguir:

- `id = expr` → asignación
- `id( ... )` → llamada a función como sentencia
- `id op expr` → expresión binaria que empieza con identificador

Se resuelve consumiendo el `IDENTIFIER` y mirando el siguiente token.

---

## Manejo de errores

Estrategia **fail-fast**: `next()` lanza `ParseException` en el primer error,
con la `Position` del token problemático. Consistente con `LexerException`.

| Situación | Mensaje |
|-----------|---------|
| Falta `:` | `Expected COLON but found 'number'` |
| Tipo desconocido | `Expected type name ('number' or 'string'), found 'boolean'` |
| Falta `;` | `Expected SEMICOLON but found 'EOF'` |
| Token inesperado | `Unexpected token ';'` |

---

## Ejemplos de AST producidos

### `let name: string = "Joe";`

```
VarDeclarationStatement
├── name: Identifier("name")
├── typeName: "string"
└── initializer: StringLiteral("Joe")
```

### `let r: number = 2 + 3 * 4;`

```
VarDeclarationStatement
└── initializer: BinaryExpression("+")
    ├── left: NumberLiteral(2.0)
    └── right: BinaryExpression("*")
        ├── left: NumberLiteral(3.0)
        └── right: NumberLiteral(4.0)
```

### `println(name + " " + lastName);`

```
ExpressionStatement
└── CallExpression("println")
    └── args[0]: BinaryExpression("+")
        ├── left: BinaryExpression("+")
        │   ├── left: Identifier("name")
        │   └── right: StringLiteral(" ")
        └── right: Identifier("lastName")
```

---

## Cómo se conecta con el resto

```
Lexer (Iterator<Token>)
  │
  ▼
ParserImpl (Iterator<Statement>)
  │
  ▼
LazyProgram (implements Program)
  │
  ├── .stream()  → Iterator<Statement>   para interpreter / formatter
  └── .toList()  → List<Statement>       para analyzer
```
