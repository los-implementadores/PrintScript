# Parser — Analizador Sintáctico

El parser consume los tokens producidos por el `Lexer` y construye el AST
representado como un `Program`. Vive en el módulo `parser`, paquete
`org.printscript.parser`.

---

## Archivos y su responsabilidad

### `Parser.java` — interfaz pública

Define el contrato del parser. Una sola operación:

```java
Program parse();
```

Uso típico:

```java
Lexer lexer = new LexerImpl(new StringReader(source));
Parser parser = new ParserImpl(lexer);
Program program = parser.parse();
```

---

### `ParseException.java` — error sintáctico

Excepción lanzada en cuanto se detecta el primer error (estrategia fail-fast).
Incluye la `Position` del token problemático para ubicar el error en el fuente.

```java
try {
    Program program = parser.parse();
} catch (ParseException e) {
    System.err.println("Syntax error: " + e.getMessage());
    // e.getPosition() → fila y columna exactas
}
```

---

### `ParserImpl.java` — implementación

Combina dos técnicas:

- **Recursivo descendente** para sentencias (`parseStatement`, `parseVarDeclaration`, etc.)
- **Pratt parsing** (top-down operator precedence) para expresiones (`parseExpression`)

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

El Pratt parser asigna un **binding power** (peso) a cada operador. Cuanto mayor
el peso, más fuerte "atrae" los operandos a su alrededor. La función central es:

```
parseExpression(minPower):
    left = parsePrimary()
    loop:
        si el operador actual tiene peso ≤ minPower → parar
        consumir operador
        right = parseExpression(peso_del_operador)   ← recursión
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

`1 + 2 + 3` se parsea como `(1 + 2) + 3`. La llamada recursiva recibe el mismo
peso del operador (`parseExpression(poder)`), lo que hace que el mismo operador
a la derecha no "entre" — necesita estrictamente mayor.

### Paréntesis

`(2 + 3) * 4` se resuelve porque `parsePrimary` consume el `(`, llama
`parseExpression(0)` recursivamente (sin restricción de peso) y luego consume
el `)`. El resultado es un `BinaryExpression(+)` que queda como operando
izquierdo del `*`.

---

## Lookahead

El parser mantiene un campo `current` con el token que está por consumir.
Cuando consume un token llama `lexer.next()` internamente. No se agrega
ningún método a la interfaz `Lexer`.

El único caso que requiere "decidir con un token de anticipación" es distinguir
`id = expr` (asignación) de `id op expr` (expresión que empieza con identificador).
Se resuelve consumiendo el `IDENTIFIER` y mirando qué sigue: si es `=`, es una
asignación; si no, se construye el nodo `Identifier` y se continúa con Pratt.

---

## Manejo de errores

Estrategia **fail-fast**: se lanza `ParseException` en el primer error con la
`Position` del token problemático. Consistente con `LexerException`.

Errores comunes:

| Situación | Mensaje |
|-----------|---------|
| Falta `:` en declaración | `Expected COLON but found 'number'` |
| Tipo desconocido | `Expected type name ('number' or 'string'), found 'boolean'` |
| Falta `;` | `Expected SEMICOLON but found 'EOF'` |
| Token inesperado en expresión | `Unexpected token ';'` |

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
├── name: Identifier("r")
├── typeName: "number"
└── initializer: BinaryExpression("+")
    ├── left: NumberLiteral(2.0)
    └── right: BinaryExpression("*")
        ├── left: NumberLiteral(3.0)
        └── right: NumberLiteral(4.0)
```

### `println(name + " " + lastName);`

```
ExpressionStatement
└── expression: CallExpression("println")
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
ParserImpl
  │  mantiene campo `current`
  │  llama lexer.next() al avanzar
  │
  ▼
Program (AST raíz)
  │
  ├── Interpreter (ASTVisitor<Object>)
  ├── Formatter   (ASTVisitor<String>)
  └── Analyzer    (ASTVisitor<List<Error>>)
```
