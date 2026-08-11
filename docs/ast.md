# AST — Abstract Syntax Tree

El AST es la representación en memoria de un programa PrintScript después de
ser parseado. Es una estructura de árbol donde cada nodo representa una
construcción del lenguaje.

Todos los nodos viven en el módulo `common`, paquete `org.printscript.common.ast`,
para que el `interpreter`, el `formatter` y el `analyzer` los puedan usar sin
depender del `parser`.

---

## Archivos y su responsabilidad

### `Node.java` — interfaz base

Todo nodo del AST implementa esta interfaz. Define dos cosas:

- `getPosition()` — la posición en el código fuente (fila/columna) de donde vino
  ese nodo. Se usa para mostrar errores con ubicación exacta.
- `accept(ASTVisitor<T>)` — punto de entrada del patrón Visitor. Cada módulo
  que quiera recorrer el árbol implementa un visitor y llama a `accept` en la raíz.

```
Node
├── getPosition() → Position
└── accept(ASTVisitor<T>) → T
```

---

### `Expression.java` — interfaz para expresiones

Marker interface que extiende `Node`. Un nodo es una `Expression` si produce
un valor al ser evaluado (un número, un string, el resultado de una operación).

Implementaciones:

| Clase | Qué representa | Ejemplo |
|---|---|---|
| `NumberLiteral` | Un número literal | `42`, `3.14` |
| `StringLiteral` | Un string literal | `"Joe"`, `'hi'` |
| `Identifier` | Referencia a una variable | `name`, `x` |
| `BinaryExpression` | Operación entre dos expresiones | `a + b`, `x / 2` |
| `CallExpression` | Llamada a función | `println(x)` |

---

### `NumberLiteral.java`

Representa un número en el código fuente. Guarda el valor como `double`
(soporta enteros y decimales).

```java
// let a: number = 42;
//                ^^— esto es un NumberLiteral con value = 42.0
new NumberLiteral(42.0, pos)
```

---

### `StringLiteral.java`

Representa un string literal. El valor es el contenido **sin** las comillas
(el lexer ya las consumió).

```java
// let name: string = "Joe";
//                    ^^^^^— StringLiteral con value = "Joe"
new StringLiteral("Joe", pos)
```

---

### `Identifier.java`

Referencia a una variable por su nombre. No sabe qué valor tiene — eso lo
resuelve el interpreter cuando lo visita y busca en el environment.

```java
// println(name)
//         ^^^^— Identifier con name = "name"
new Identifier("name", pos)
```

---

### `BinaryExpression.java`

Operación entre dos expresiones. El operador es el símbolo como string
(`"+"`, `"-"`, `"*"`, `"/"`).

El árbol codifica la precedencia: `2 + 3 * 4` se representa como:

```
BinaryExpression(+)
├── NumberLiteral(2)
└── BinaryExpression(*)
    ├── NumberLiteral(3)
    └── NumberLiteral(4)
```

```java
// a / b
new BinaryExpression(
    new Identifier("a", pos),
    "/",
    new Identifier("b", pos),
    pos
)
```

---

### `CallExpression.java`

Llamada a función. En PrintScript 1.0 la única función es `println`, pero
el nodo es genérico.

- `callee` — nombre de la función (`"println"`)
- `arguments` — lista inmutable de expresiones que se le pasan

```java
// println(name + " " + lastName)
new CallExpression("println", List.of(
    new BinaryExpression(
        new BinaryExpression(new Identifier("name", pos), "+", new StringLiteral(" ", pos), pos),
        "+",
        new Identifier("lastName", pos),
        pos
    )
), pos)
```

---

### `Statement.java` — interfaz para sentencias

Marker interface que extiende `Node`. Un nodo es un `Statement` si es una
instrucción completa (termina en `;`). No produce un valor por sí mismo.

Implementaciones:

| Clase | Qué representa | Ejemplo |
|---|---|---|
| `VarDeclarationStatement` | Declaración de variable | `let x: number = 5;` |
| `AssignmentStatement` | Reasignación de variable existente | `x = 5;` |
| `ExpressionStatement` | Una expresión usada como sentencia | `println(x);` |

---

### `VarDeclarationStatement.java`

Representa `let nombre: tipo = expresión;`. Tiene tres partes:

- `name` — un `Identifier` con el nombre de la variable
- `typeName` — el tipo como string: `"number"` o `"string"`
- `initializer` — la expresión del lado derecho del `=`

```java
// let name: string = "Joe";
new VarDeclarationStatement(
    new Identifier("name", pos),
    "string",
    new StringLiteral("Joe", pos),
    pos
)
```

---

### `AssignmentStatement.java`

Representa `variable = expresión;` sobre una variable **ya declarada**.
Distinto de `VarDeclarationStatement` que también la declara.

- `target` — el `Identifier` de la variable que se actualiza
- `value` — la nueva expresión

```java
// a = a / b;
new AssignmentStatement(
    new Identifier("a", pos),
    new BinaryExpression(new Identifier("a", pos), "/", new Identifier("b", pos), pos),
    pos
)
```

---

### `ExpressionStatement.java`

Una expresión sola usada como sentencia. En PrintScript 1.0 se usa
exclusivamente para `println(...)`.

- `expression` — la expresión que forma la sentencia (típicamente un `CallExpression`)

```java
// println(x);
new ExpressionStatement(
    new CallExpression("println", List.of(new Identifier("x", pos)), pos),
    pos
)
```

---

### `Program.java` — raíz del árbol (interfaz)

El nodo raíz. Representa el programa completo como una secuencia de `Statement`.
Es una **interfaz** con dos métodos:

- `toList()` — materializa y devuelve todas las sentencias como lista inmutable.
  Usar cuando se necesita el árbol completo (p.ej. el analyzer).
- `stream()` — devuelve un `Iterator<Statement>` lazy. Usar cuando se procesa
  de a uno sin necesitar todo en memoria (p.ej. el interpreter).

Hay dos implementaciones:

| Clase | Cuándo usar |
|---|---|
| `EagerProgram` | Lista inmutable cargada en construcción. Para tests y analyzer. |
| `LazyProgram` | Envuelve un `Iterator<Statement>` (el parser). Drena on-demand. |

```java
// Eager — árbol completo en memoria desde el inicio:
Program program = new EagerProgram(List.of(decA, decB, printStmt), pos);
program.toList();   // [VarDecl, VarDecl, ExprStmt]

// Lazy — envuelve el parser, drena cuando hace falta:
Parser parser = new ParserImpl(lexer);
Program program = new LazyProgram(parser, pos);
program.stream();   // Iterator<Statement> que avanza el parser de a uno
program.toList();   // drena el iterator completo y cachea el resultado
```

---

### `ASTVisitor.java` — interfaz del patrón Visitor

Cada módulo que necesita recorrer el árbol implementa esta interfaz.
El tipo genérico `<T>` es el tipo de retorno de cada visita.

```
ASTVisitor<T>
├── visitProgram(Program)                → T
├── visitVarDeclaration(VarDeclaration)  → T
├── visitAssignment(Assignment)          → T
├── visitExpressionStatement(...)        → T
├── visitBinaryExpression(...)           → T
├── visitCallExpression(...)             → T
├── visitNumberLiteral(...)              → T
├── visitStringLiteral(...)              → T
└── visitIdentifier(...)                 → T
```

Ejemplos de uso por módulo:

| Módulo | `<T>` | Qué hace |
|---|---|---|
| `interpreter` | `Object` | evalúa y retorna el valor |
| `formatter` | `String` | retorna el código formateado |
| `analyzer` | `List<AnalyzerError>` | retorna los errores encontrados |

Para recorrer el árbol, el módulo implementa el visitor y arranca desde la raíz:

```java
ASTVisitor<Object> interpreter = new InterpreterVisitor(env);
program.accept(interpreter); // arranca el recorrido
```

---

## Cómo se conectan

```
                    common/ast
                    ──────────
Lexer               Node  (interfaz base)
  │                  ├── Expression
  │ Iterator<Token>  │    ├── NumberLiteral
  ▼                  │    ├── StringLiteral
Parser               │    ├── Identifier
  │                  │    ├── BinaryExpression
  │ Iterator<Stmt>   │    └── CallExpression
  ▼                  └── Statement
LazyProgram               ├── VarDeclarationStatement
  │ implements            ├── AssignmentStatement
  │ Program               └── ExpressionStatement
  │                  Program  (interfaz raíz)
  ├── .stream()       ├── EagerProgram  (lista inmutable)
  │   Iterator<Stmt>  └── LazyProgram   (drena parser on-demand)
  └── .toList()       ASTVisitor<T>  (interfaz)
      List<Stmt>
  │
  ├── Interpreter
  │   implements
  │   ASTVisitor<Object>
  │
  ├── Formatter
  │   implements
  │   ASTVisitor<String>
  │
  └── Analyzer
      implements
      ASTVisitor<List<Error>>
```

El `Parser` produce un `Program`. Los módulos posteriores lo reciben y lo
recorren cada uno con su propio `ASTVisitor`, sin tocarse entre sí.
