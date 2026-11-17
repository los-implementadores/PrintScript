# AST — Abstract Syntax Tree

El AST es la representación en memoria de un programa PrintScript después de
ser parseado. Es una estructura de árbol donde cada nodo representa una
construcción del lenguaje.

Todos los nodos viven en el módulo `ast`, paquete `org.printscript.ast`, para que
el `interpreter`, el `analyzer` y el `formatter` los puedan usar sin depender
del `parser`. El módulo solo depende de `source` (por `Position`).

El AST guarda lo que hace falta para escribir el programa como estaba (ADR-029):
los paréntesis (`GroupExpression`) y el texto de los literales (`getLexeme()`).
Los espacios no: el formatter los toma del código original (ver
[formatter.md](formatter.md)).

Ningún nodo sabe de versiones del lenguaje: qué construcciones se aceptan en cada
versión lo decide el runner (ADR-025).

---

## Archivos y su responsabilidad

### `Node.java` — interfaz base

Todo nodo del AST implementa esta interfaz. Define tres cosas:

- `getPosition()` — la posición en el código fuente (fila/columna) de donde vino
  ese nodo. Se usa para mostrar errores con ubicación exacta.
- `children()` — las expresiones, sentencias y bloques que contiene, en el orden
  del código fuente. Permite recorrer el árbol sin conocer cada tipo de nodo (lo
  usa el linter). No incluye el nombre de una declaración ni el destino de una
  asignación: así, todo `Identifier` que aparece entre los hijos es una lectura.
- `accept(ASTVisitor<T>)` — punto de entrada del patrón Visitor, para las fases
  que tienen que hacer algo con cada tipo de nodo (semántico, intérprete y
  formatter).

```
Node
├── getPosition() → Position
├── children()    → List<Node>
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
| `BooleanLiteral` | Un booleano literal | `true`, `false` |
| `Identifier` | Referencia a una variable | `name`, `x` |
| `BinaryExpression` | Operación entre dos expresiones | `a + b`, `x / 2` |
| `CallExpression` | Llamada a función | `println(x)` |
| `GroupExpression` | Expresión entre paréntesis | `(a + b)` |

---

### `NumberLiteral.java`

Representa un número en el código fuente. Guarda el valor como `double`
(soporta enteros y decimales) y el lexema, el número tal como se escribió: `5.0`
y `5` valen lo mismo pero no se escriben igual, y el formatter tiene que
respetarlo.

```java
// let a: number = 1.50;
//                 ^^^^— NumberLiteral con value = 1.5 y lexeme = "1.50"
new NumberLiteral(1.5, "1.50", pos)
new NumberLiteral(42.0, pos)          // lexeme = "42"
```

---

### `StringLiteral.java`

Representa un string literal. El valor es el contenido **sin** las comillas;
el lexema es el literal tal como se escribió, comillas incluidas. Así el
formatter no cambia `'Joe'` por `"Joe"`.

```java
// let name: string = 'Joe';
//                    ^^^^^— StringLiteral con value = "Joe" y lexeme = "'Joe'"
new StringLiteral("Joe", "'Joe'", pos)
new StringLiteral("Joe", pos)         // lexeme = "\"Joe\""
```

---

### `BooleanLiteral.java`

Representa `true` o `false`. Guarda el valor como `boolean`.

```java
// let ok: boolean = true;
//                   ^^^^— BooleanLiteral con value = true
new BooleanLiteral(true, pos)
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

### `GroupExpression.java`

Una expresión entre paréntesis. Para evaluar no hace falta (la precedencia ya
quedó en la forma del árbol), pero sin este nodo el formatter escribiría
`(1 + 2) * 3` como `1 + 2 * 3`, que es otro programa. Es el `ExprKind::Paren` del
AST de Rust (ADR-029). El semántico y el intérprete lo resuelven con la expresión
de adentro.

```
// (1 + 2) * 3
BinaryExpression(*)
├── GroupExpression
│   └── BinaryExpression(+)
│       ├── NumberLiteral(1)
│       └── NumberLiteral(2)
└── NumberLiteral(3)
```

`GroupExpression.unwrap(expr)` saca todos los paréntesis: `((x))` da `x`. Lo usa
el linter, para que `println((x))` siga siendo un argumento simple.

---

### `CallExpression.java`

Llamada a función: `println`, `readInput` y `readEnv` son todas `CallExpression`,
sin nodo propio (ver ADR-010 y ADR-011). Qué funciones existen en cada versión lo
decide el runner.

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
instrucción completa (termina en `;`, salvo el `if`, que termina en `}`). No
produce un valor por sí mismo.

Implementaciones:

| Clase | Qué representa | Ejemplo |
|---|---|---|
| `VarDeclarationStatement` | Declaración de variable | `let x: number = 5;` |
| `AssignmentStatement` | Reasignación de variable existente | `x = 5;` |
| `ExpressionStatement` | Una expresión usada como sentencia | `println(x);` |
| `IfStatement` | Condicional con bloques | `if (ok) { println(1); }` |

---

### `VarDeclarationStatement.java`

Representa `let nombre: tipo = expresión;` y `const nombre: tipo = expresión;`.
Tiene cuatro partes:

- `name` — un `Identifier` con el nombre de la variable
- `typeName` — el tipo como string: `"number"`, `"string"` o `"boolean"`
- `initializer` — la expresión del lado derecho del `=`. Es `null` en un `let` sin
  valor inicial; en un `const` es obligatoria
- `isConst` — `true` si se declaró con `const`

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

Una expresión sola usada como sentencia. En la práctica, una llamada como
`println(...)`.

- `expression` — la expresión que forma la sentencia (típicamente un `CallExpression`)

```java
// println(x);
new ExpressionStatement(
    new CallExpression("println", List.of(new Identifier("x", pos)), pos),
    pos
)
```

---

### `IfStatement.java`

Representa `if (condición) { ... } else { ... }`. El `else` es opcional y no
existe `else if`.

- `condition` — la expresión entre paréntesis (el semántico exige que sea `boolean`)
- `thenBlock` — el `Block` del `if`
- `elseBlock` — el `Block` del `else`, o `null` si no hay (`hasElse()` lo indica)

```java
// if (ok) { println("si"); } else { println("no"); }
new IfStatement(
    new Identifier("ok", pos),
    new Block(List.of(printSi), pos),
    new Block(List.of(printNo), pos),
    pos
)
```

---

### `Block.java`

Un bloque de sentencias entre llaves, `{ ... }`. Abre un scope: lo que se declara
adentro deja de existir al salir (ADR-022). Es un nodo propio para que cualquier
construcción nueva con cuerpo (un `while`, por ejemplo) lo reuse, y con eso el
semántico, el intérprete y el linter ya manejen su scope sin cambios.

- `statements` — lista inmutable de sentencias del bloque

---

### `Program.java` — el programa completo (interfaz)

Representa el programa completo como una secuencia de `Statement`. No es un nodo
del árbol sino la fuente de sus sentencias (no tiene posición ni hijos). Es una
**interfaz** con un solo método:

- `stream()` — devuelve un `Iterator<Statement>`. El contrato es que un `Program`
  se recorre **una sola vez** (ver ADR-020): quien necesite varias pasadas se
  guarda las sentencias él mismo.

Hay dos implementaciones:

| Clase | Cuándo usar |
|---|---|
| `LazyProgram` | Envuelve un `Iterator<Statement>` (el parser). Drena on-demand; un segundo `stream()` lanza `IllegalStateException`. |
| `EagerProgram` | Envuelve una lista ya armada. Para tests. |

```java
// Lazy — envuelve el parser, drena cuando hace falta:
Program program = new LazyProgram(parser);
program.stream();   // Iterator<Statement> que avanza el parser de a uno

// Eager — programa armado a mano:
Program program = new EagerProgram(List.of(decA, decB, printStmt));
program.stream();   // itera sobre la lista
```

---

### `ASTVisitor.java` — interfaz del patrón Visitor

Lo implementan las fases que le dan un significado propio a **cada** tipo de
nodo. El tipo genérico `<T>` es el tipo de retorno de cada visita.

```
ASTVisitor<T>
├── visitVarDeclaration(VarDeclaration)  → T
├── visitAssignment(Assignment)          → T
├── visitExpressionStatement(...)        → T
├── visitIf(IfStatement)                 → T
├── visitBlock(Block)                    → T
├── visitBinaryExpression(...)           → T
├── visitCallExpression(...)             → T
├── visitGroup(GroupExpression)          → T
├── visitNumberLiteral(...)              → T
├── visitStringLiteral(...)              → T
├── visitBooleanLiteral(...)             → T
└── visitIdentifier(...)                 → T
```

| Módulo | Cómo recorre | Por qué |
|---|---|---|
| `interpreter` (`InterpreterVisitor`) | `ASTVisitor<Object>` | evalúa cada nodo y retorna su valor |
| `interpreter` (`SemanticAnalyzerVisitor`) | `ASTVisitor<String>` | retorna el tipo de cada expresión |
| `analyzer` | `children()` | cada regla mira solo algunos nodos; un nodo nuevo no la obliga a cambiar |
| `formatter` (`FormatterVisitor`) | `ASTVisitor<Void>` | escribe cada nodo con sus partes y le pasa a las reglas cada espacio entre ellas |

El visitor obliga a agregar un `visitX` en cada implementación cuando aparece un
nodo nuevo (el *expression problem*, ADR-009). Para el semántico, el intérprete y
el formatter es lo correcto: un nodo nuevo necesita su chequeo, su ejecución y
saber cómo se escribe (ADR-029). Para el linter no lo es, y por eso no lo usa
(ADR-027).

```java
// Cada sentencia se visita de a una, a medida que el parser la produce:
statement.accept(interpreterVisitor);
```

---

## Cómo se conectan

```
                    ast
                    ───
Lexer               Node  (interfaz base)
  │                  ├── Expression
  │ Iterator<Token>  │    ├── NumberLiteral
  ▼                  │    ├── StringLiteral
                     │    ├── BooleanLiteral
Parser               │    ├── Identifier
  │                  │    ├── BinaryExpression
  │ Iterator<Stmt>   │    └── CallExpression
  ▼                  └── Statement
LazyProgram               ├── VarDeclarationStatement
  │ implements            ├── AssignmentStatement
  │ Program               ├── ExpressionStatement
  │                       └── IfStatement
  │                  Block    (cuerpo de un if)
  │                  Program  (interfaz raíz)
  └── .stream()       ├── EagerProgram  (lista armada, tests)
      Iterator<Stmt>  └── LazyProgram   (drena parser on-demand)
      (una pasada)    ASTVisitor<T>  (interfaz)
  │
  ├── Interpreter
  │   implements
  │   ASTVisitor<Object>
  │
  ├── SemanticAnalyzer
  │   implements
  │   ASTVisitor<String>
  │
  └── Analyzer
      recorre con
      children()
```

El `Parser` produce un `Program`. El semántico y el intérprete lo recorren cada
uno con su propio `ASTVisitor`; el linter, con `children()`. Ninguno toca a los
otros.
