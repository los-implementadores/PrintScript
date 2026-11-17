# ADR-029: El formatter trabaja sobre el AST, con el código original como referencia

- **Estado:** Aceptado
- **Fecha:** 2026-11-17
- **Relacionado:** ADR-002 (Visitor), ADR-009 (limitación OCP del visitor), ADR-020 (`Program` de una sola pasada), ADR-023 (formatting en streaming), ADR-026 (registro de reglas, se mantiene), ADR-027 (reemplaza la parte del formatter)

## Contexto

ADR-027 pasó el formatter a trabajar sobre tokens. El lexer devuelve los espacios como tokens
(`LexerFactory.createWithWhitespace`), el runner los valida con el parser (`ParsedTokens`) y
`FormatterImpl` le pasa a cada `FormattingRule` el espacio entre dos tokens (`Gap`). Los motivos
eran tres:

1. Que un nodo nuevo del lenguaje no obligara a tocar el formatter.
2. Que lo que ninguna regla pide cambiar quede como estaba, que es lo que exige el TCK.
3. Tener un solo formateador para la CLI y el TCK, en vez de dos con reglas distintas.

En la corrección, la cátedra marcó que **el modelo de configuración es correcto** (ADR-026:
`RuleConfig`, `RuleRegistry` y el registro `FormattingRules` con las claves propias y las del TCK),
pero que **el formatter tiene que trabajar sobre el AST**, no sobre un iterador de tokens, por dos
razones:

- **La dependencia con el parser existe igual.** Formatear ya requiere parsear: `ParsedTokens` arma
  el parser sobre los mismos tokens para validar la sintaxis. El diseño actual parsea, tira el AST
  (`statements.next()` sin usar el resultado) y después cada regla vuelve a deducir la estructura
  del programa mirando tokens.
- **Los tokens limitan lo que puede hacer el formatter.** Sobre tokens las reglas no ven el
  lenguaje: ven una secuencia de símbolos. Lo que no está en la secuencia (qué es una declaración,
  dónde empieza un bloque, qué agrupa una operación) cada regla lo tiene que reconstruir, y lo que
  no se puede reconstruir no se puede formatear.

Este ADR toma como referencia a **rustfmt**, el formatter oficial de Rust, que resolvió estos mismos
problemas: formatea el AST del compilador sin perder lo que el programador escribió.

## Problemas del formatter sobre tokens

### 1. Cada regla vuelve a parsear

Las reglas sobre tokens deducen la gramática a partir de los tokens, cada una a su manera:

| Regla | Qué necesita saber | Cómo lo deduce de los tokens | Qué ya dice el AST |
|---|---|---|---|
| `BlockIndentation` | En cuántos bloques está la línea | Cuenta `{` y `}` y guarda la profundidad como estado | Los `Block` anidados |
| `BlankLinesAfterPrintln` | Si la sentencia anterior es un `println` | Guarda `inPrintln`; decide que empieza una sentencia si el token anterior es `;`, `{` o `}`, y que es un `println` si el primer token es el identificador `println` | `ExpressionStatement(CallExpression("println"))` |
| `BlankLinesBeforePrintln` | Si la sentencia siguiente es un `println` | El token anterior es `;` o `}` y el siguiente es el identificador `println` | Ídem |
| `BracePlacement` | Dónde está la llave de un `if` o un `else` | Toma cualquier `{` | `IfStatement.getThenBlock()` y `getElseBlock()` |
| `LineBreakAfterStatement` | Dónde termina una sentencia | Toma cualquier `;` | Cada `Statement` |
| `TokenSpacing` (`:`, `=`, operadores) | Si el `:` es el de una declaración, si el `-` es una resta | Toma cualquier `COLON`, `ASSIGN` o `MINUS` | `VarDeclarationStatement`, `BinaryExpression` |

Esto es conocimiento de la gramática duplicado fuera del parser, que es su único dueño. Funcionaba
porque PrintScript 1.1 es chico y cada símbolo tiene un solo significado. Las mismas claves del TCK
lo dicen: `enforce-spacing-before-colon-in-declaration` habla de un `:` **en una declaración**, que
es un concepto del AST. La regla de tokens equivalente, `TokenSpacing.before(COLON)`, se aplica a
**cualquier** `:`.

### 2. El lenguaje que crece las rompe en silencio

ADR-027 sacó el formatter del visitor porque lo que más crece en PrintScript son las construcciones
del lenguaje. Pero sobre tokens una construcción nueva no obliga a tocar el formatter; lo rompe sin
avisar. Compila, los tests viejos pasan y la salida queda mal:

- Un `for (let i: number = 0; i < 10; i = i + 1)` tiene dos `;` que no terminan una sentencia:
  `LineBreakAfterStatement` corta la línea adentro de los paréntesis.
- Un menos unario (`let y: number = -x;`) es un `MINUS` como el de la resta:
  `spaceAroundOperators` escribe `- x`.
- Cualquier `{` que no abra el bloque de un `if` (el cuerpo de una función, un literal de objeto) lo
  toman `BracePlacement` y `BlockIndentation` como si lo fuera.

Sobre el AST, un nodo nuevo no compila hasta que el printer sepa escribirlo (ADR-009). El error
aparece en compilación, no en la salida de un usuario.

### 3. El orden de las reglas es un acoplamiento escondido

Todas las reglas escriben sobre el mismo espacio y el resultado depende del orden en que se
registran: primero las que cortan líneas, después los espacios dentro de una línea y al final la
indentación, que corrige lo que dejaron las anteriores. Ese orden está escrito en un comentario de
`FormattingRules`. Por ejemplo, `BracePlacement(false)` baja la llave con la indentación de la línea
del `if`, y `BlockIndentation` la tiene que corregir después. Quien agrega una regla tiene que saber
dónde meterla para no pisar ni ser pisada.

### 4. Hay reglas que no se pueden escribir

Algunas reglas de formato necesitan el árbol y no hay forma de expresarlas sobre tokens:

- **Ancho máximo de línea.** Para cortar un `println("..." + a + "...")` largo hay que saber cómo
  está agrupada la expresión, dónde se puede cortar y con qué indentación sigue. Es la opción
  principal de rustfmt (`max_width`), y `binop_separator` decide si el operador va al final o al
  principio de la línea cortada.
- **Paréntesis.** Saber si un paréntesis sobra requiere conocer la precedencia de los operadores
  (en rustfmt, `remove_nested_parens`).
- **Reglas sobre construcciones**: una línea vacía antes de cada `if`, el `else` en la misma línea
  que la `}`, alinear el `=` de declaraciones seguidas o dejar en una línea un `if` de una sola
  sentencia.

## Por qué no alcanzaba el `FormatterVisitor` anterior

Antes del PR #62 ya hubo un formatter sobre el AST (`FormatterVisitor`, la estrategia 1 del
ADR-012). Se reemplazó porque tenía tres problemas. Ninguno era propio del AST:

1. **Regeneraba todo desde cero.** Cada espacio salía de un valor por defecto, así que destruía lo
   que ninguna regla pedía cambiar. El caso `enforce-decl-spacing-after-colon` del TCK lo muestra:
   con la misma configuración, el resultado esperado tiene `something: string = "..."` en una línea y
   `third_thing : string="..."` en otra. Ningún printer que regenere todo puede producir esas dos
   líneas, porque el espacio antes del `:` y alrededor del `=` sale del original. **Hace falta el
   texto original.**
2. **El AST perdía información, y el printer cambiaba el significado del programa:**
   - `GroupParselet` devuelve la expresión de adentro y no deja rastro de los paréntesis.
     `(1 + 2) * 3` salía como `1 + 2 * 3`, que da 7 en vez de 9.
   - `NumberLiteral` guarda un `double`: `5.0` salía `5` y `1.50` salía `1.5`.
   - `StringLiteral` guarda el valor sin comillas y el printer usaba siempre `"`. El string
     `'dijo "hola"'` salía como `"dijo "hola""`, que ni siquiera es un string válido.
3. **Las reglas estaban mezcladas con la impresión.** Había `if (rules.isSpaceAroundOperatorsAst())`
   dentro de `visitBinaryExpression`, `") {\n"` fijo en `visitIf` (por eso no soportaba
   `if-brace-below-line`) y `", "` fijo entre argumentos. Agregar una regla era tocar el visitor.

El problema no era el AST. Era un AST sin el texto original y sin la información que necesita un
formatter, y un printer con las reglas adentro. rustfmt resuelve exactamente eso.

## Cómo lo resuelve rustfmt

rustfmt formatea el AST de `rustc` (`rustc_ast`). En su documento de diseño (`Design.md`) se
compara con un formatter sobre tokens. Concluye que el AST da más contexto para tomar decisiones y
permite transformaciones más sofisticadas. También advierte que un formatter de tokens necesita su
propio parser, más simple, y que a medida que crece termina siendo la misma herramienta. Es lo que
les pasa a nuestras reglas: cada una tiene su pedacito de parser.

Las piezas que importan para PrintScript:

- **Un printer por construcción.** Cada nodo implementa el trait `Rewrite`:

  ```rust
  fn rewrite(&self, context: &RewriteContext<'_>, shape: Shape) -> Option<String>;
  ```

  `Shape` es el espacio disponible: el ancho (`width`), la indentación (`indent`, con
  `block_indent` y `alignment`) y cuánto ya se escribió en la línea (`offset`). `RewriteContext`
  lleva la configuración (`config`) y el acceso al código original (`snippet_provider`).

- **El código original nunca se pierde.** Cada nodo del AST tiene su `Span`, que indica dónde
  empieza y termina en el archivo. El visitor de rustfmt lleva un cursor (`last_pos`) con hasta
  dónde ya escribió el original. Entre un nodo y el siguiente, `format_missing(end)` copia lo que
  hay en el original entre `last_pos` y `end`, que pueden ser comentarios o líneas vacías. Las
  líneas vacías se respetan, limitadas por `blank_lines_lower_bound` y `blank_lines_upper_bound`.
  Lo que el AST no formatea, se copia.

- **Lo que el AST no guarda, se busca en el original.** El AST no tiene la posición de cada `:` o
  `=`. rustfmt la busca en el texto original, dentro del span del nodo, con `SpanUtils`
  (`span_after(span, ":")`, `span_before(span, "=")`).

- **El AST guarda lo necesario para no cambiar el programa.** `rustc_ast` tiene un nodo para los
  paréntesis, `ExprKind::Paren`, documentado como «No-op: used solely so we can pretty-print
  faithfully». Los literales guardan su texto sin interpretar (`symbol`) además de su tipo
  (`kind`, en `token::Lit`), así `1.50` sigue siendo `1.50`. Y `remove_nested_parens` saca solo los
  paréntesis anidados (`((a))`), no los que el programador puso a propósito.

- **No hacer daño.** Si un `rewrite` no puede formatear un nodo, devuelve `None`. El que lo llamó
  prueba otra forma y, si ninguna sirve, deja el código original. El principio del documento de
  diseño es que rustfmt nunca debe empeorar código que estaba bien.

- **La configuración es independiente del recorrido.** Las opciones (`rustfmt.toml`) se declaran
  una vez en `create_config!` (nombre, tipo, si es estable y descripción). El código de formateo
  las lee de `context.config`. Varias son casi las mismas reglas que pide PrintScript:
  `space_before_colon`, `space_after_colon`, `control_brace_style` (`AlwaysSameLine`,
  `AlwaysNextLine`, `ClosingNextLine`), `tab_spaces` y `blank_lines_upper_bound`.

- **Tests de ida y de idempotencia.** `system_tests` formatea `tests/source` y compara con
  `tests/target`, e `idempotence_tests` formatea `tests/target` y exige que no cambie nada.

**Dos diferencias con PrintScript:**

- rustfmt es **canónico**: cada opción tiene un valor por defecto y la salida no depende de los
  espacios originales (salvo líneas vacías y comentarios). El TCK pide lo contrario: lo que no se
  configura queda como estaba. Por eso tomamos el mecanismo de `format_missing` (copiar el original
  entre nodos) y lo llevamos a **cada** espacio del programa, no solo al que hay entre sentencias.
- rustfmt carga el archivo entero en memoria. Nosotros seguimos formateando de a una sentencia
  (ADR-020, ADR-023).

## Decisión

### 1. El formatter recibe sentencias del AST y el texto original

```java
public interface Formatter {
  void format(Iterator<Statement> statements, SourceText source, Writer target);
}
```

- `formatter` pasa a depender de `ast`, `source` y `rules`, igual que el linter. Deja de depender
  de `token`.
- `SourceText` (módulo `source`) es el equivalente al `snippet_provider` de rustfmt: el código
  original. Se recorre con un cursor, como el `last_pos` de rustfmt: `whitespace()` devuelve los
  espacios que hay en el cursor y `expect(texto)` avanza sobre una parte que ya se conoce, y falla
  si el original no sigue con eso.

### 2. El AST guarda lo que el formatter necesita para no cambiar el programa

Como en `rustc_ast`:

- **Paréntesis:** un nodo nuevo, `GroupExpression`, que el parser arma en `GroupParselet`. Es el
  `ExprKind::Paren` de Rust. El semántico y el intérprete lo resuelven delegando en la expresión de
  adentro, y en el linter es un hijo más (`children()`).
- **Literales con su texto:** `NumberLiteral` y `StringLiteral` guardan el lexema tal como se
  escribió (comillas incluidas) además del valor, como `symbol` y `kind` en `token::Lit`. El
  intérprete sigue usando el valor y el formatter usa el lexema.
- **No hacen falta posiciones nuevas.** Cada parte de un nodo es un texto que el formatter ya
  conoce: las palabras reservadas y los símbolos de cada construcción (`let`, `:`, `=`, `if (`,
  `{`), los nombres, el tipo tal como se escribió y el lexema de los literales. En vez de buscar
  cada símbolo con `span_after`, el formatter avanza el cursor parte por parte y entre una y otra
  solo puede haber espacio, porque PrintScript no tiene comentarios. Si el AST y el original no
  coinciden, `expect` falla en vez de escribir otro programa.

### 3. Un printer por nodo, con el visitor

`FormatterVisitor implements ASTVisitor<Void>` escribe cada nodo, como el `Rewrite` de rustfmt:
un método por nodo, que sabe qué partes tiene y en qué orden. `FormattedOutput` hace de
`RewriteContext`: lleva el cursor del original, las reglas, la profundidad de bloques y la
indentación de la línea actual, que le llegan a cada regla en un `Shape` (en el futuro, también el
ancho disponible).

El formatter vuelve al grupo del semántico y el intérprete (ADR-009, ADR-027): cada nodo tiene
sintaxis que imprimir, así que tocar el printer cuando aparece un nodo nuevo es parte del trabajo,
no un costo accidental. El compilador marca qué nodos faltan.

### 4. Las brechas son del AST, no de los tokens

El printer escribe el texto de cada nodo y, entre cada par de partes, arma una brecha (`Gap`) que
dice **qué espacio es** en términos del lenguaje:

```java
public record Gap(GapKind kind, Node node, Node previous, String whitespace, Shape shape) {}
```

- `kind`: qué espacio es (tabla de abajo).
- `node`: el nodo al que pertenece (la declaración, la operación, el `if`) o, entre sentencias, la
  sentencia que sigue.
- `previous`: entre sentencias, la sentencia anterior.
- `whitespace`: el espacio original, sacado de `SourceText`, o el que dejó la regla anterior.
- `shape`: la profundidad de bloques y la indentación.

| `GapKind` | Dónde está | Claves que lo deciden |
|---|---|---|
| `BEFORE_TYPE_COLON` | `let x␣: number` | `spaceBeforeColon`, `enforce-spacing-before-colon-in-declaration` |
| `AFTER_TYPE_COLON` | `let x:␣number` | `spaceAfterColon`, `enforce-spacing-after-colon-in-declaration` |
| `BEFORE_ASSIGN` / `AFTER_ASSIGN` | `x␣=␣5`, en declaraciones y asignaciones | `spaceAroundAssign`, `enforce-spacing-around-equals`, `enforce-no-spacing-around-equals` |
| `BEFORE_OPERATOR` / `AFTER_OPERATOR` | `a␣+␣b` | `spaceAroundOperators`, `mandatory-space-surrounding-operations` |
| `BEFORE_BLOCK` | `if (x)␣{`, `else␣{` | `if-brace-same-line`, `if-brace-below-line` |
| `BETWEEN_STATEMENTS` | `;␣println(...)`: saltos de línea, líneas vacías e indentación | `mandatory-line-break-after-statement`, `newlineBeforePrintln`, `line-breaks-after-println`, `indentSize` / `indent-inside-if` |
| `BLOCK_START` / `BLOCK_END` | `{␣primera sentencia` y `última sentencia␣}` | `mandatory-line-break-after-statement` (solo `BLOCK_END`), `indentSize` / `indent-inside-if` |
| `FILE_START` / `FILE_END` | Antes de la primera sentencia / después de la última | `indentSize` / `indent-inside-if` (saca la indentación del principio) |
| `AFTER_KEYWORD`, `BEFORE_SEMICOLON`, `BEFORE_ARGUMENTS`, `INSIDE_PARENS`, `BEFORE_COMMA`, `AFTER_COMMA`, `BEFORE_ELSE` | `let␣x`, `x␣;`, `println␣(`, `(␣x␣)`, `}␣else` | `maxOneSpaceBetweenTokens`, `mandatory-single-space-separation` (que deja sin espacio solo `BEFORE_SEMICOLON`) |

Las dos últimas reglas de la tabla se aplican a todas las brechas que no cortan la línea.

### 5. El modelo de configuración no cambia

`RuleConfig`, `RuleRegistry` y el registro `FormattingRules` quedan como están (ADR-026): las mismas
claves, los mismos alias del TCK y las mismas fábricas (`whenOn`, `withBoolean`, `withInt`). Una
regla sigue siendo `String apply(Gap gap)`, y agregar una sigue siendo escribir su clase y una línea
en el registro. Lo que cambia es lo que ve la regla:

```java
// Antes, sobre tokens: estado propio y un mini-parser
if (startsStatement(gap)) {
  inPrintln = gap.isBeforeIdentifier("println");
}

// Ahora, sobre el AST (BlankLinesAfterPrintln): la sentencia anterior es un dato de la brecha
if (gap.kind() == GapKind.BETWEEN_STATEMENTS
    && gap.breaksLine()
    && PrintlnStatement.is(gap.previous())) {
  return gap.newLine(lines);
}
```

- **Ninguna regla guarda estado ni reconstruye la gramática.** La profundidad sale de
  `shape.depth()` y la sentencia anterior, de `gap.previous()`.
- **Cada regla mira solo las brechas que le corresponden**, así que casi no hay reglas que se pisen.
  Cuando dos tocan la misma brecha (por ejemplo `maxOneSpaceBetweenTokens` y `spaceAfterColon`), el
  orden del registro sigue decidiendo, como antes.

### 6. Lo que ninguna regla decide, se copia del original

Es el `format_missing` de rustfmt aplicado a cada brecha. `gap.whitespace()` arranca con el texto
original y, si ninguna regla lo cambia, se escribe así. Sin reglas, la salida es idéntica a la
entrada, como pide el TCK. Las líneas vacías entre sentencias se respetan por la misma vía.

### 7. Sigue siendo streaming, sin tokens de espacio

El runner arma el formatting igual que el análisis: `LazyProgramLoader` produce las sentencias de a
una. Además envuelve el `Reader` en un `SourceText` que guarda lo que el lexer va leyendo y descarta
lo que ya se formateó. En memoria queda una sentencia más el token que el parser leyó de más, como
antes. Un error de sintaxis frena el formateo porque la sentencia no llega a existir (ADR-023).

Se borran `ParsedTokens`, `TokenLoader`, `ParsedTokenLoader`, `WhitespaceTokenMatcher`,
`LexerFactory.createWithWhitespace` y `TokenType.WHITESPACE`. El lexer vuelve a tener una sola
configuración, y qué es un espacio lo define `SourceText.isWhitespace`: el lexer descarta y el
formatter copia exactamente los mismos caracteres.

`FormattingService.format(Reader, LanguageVersion, RuleConfig, Writer)` no cambia de firma, así que
la CLI y el adapter del TCK no se enteran.

## Ejemplo

Con `{"enforce-spacing-after-colon-in-declaration": true}` y la última línea del caso
`enforce-decl-spacing-after-colon` del TCK (con el string acortado), `let third_thing :string="x";`:

| Parte | Brecha | Original | Regla | Sale |
|---|---|---|---|---|
| `let` → `third_thing` | `AFTER_KEYWORD` | `␣` | ninguna | `␣` |
| `third_thing` → `:` | `BEFORE_TYPE_COLON` | `␣` | ninguna | `␣` |
| `:` → `string` | `AFTER_TYPE_COLON` | (nada) | `spaceAfterColon` | `␣` |
| `string` → `=` | `BEFORE_ASSIGN` | (nada) | ninguna | (nada) |
| `=` → `"x"` | `AFTER_ASSIGN` | (nada) | ninguna | (nada) |

Resultado: `let third_thing : string="x";`, igual al golden del TCK. El string sale con su lexema,
así que mantiene sus comillas. Y `(1 + 2) * 3` sale con sus paréntesis, porque ahora son un nodo.

## Alternativas consideradas

1. **Seguir sobre tokens (ADR-027).** Se descarta por los problemas de arriba: la dependencia con
   el parser existe igual, las reglas duplican la gramática, el lenguaje que crece las rompe en
   silencio y hay reglas que no se pueden escribir.
2. **Volver al `FormatterVisitor` canónico, que regenera todo.** Es rustfmt sin `format_missing`.
   No puede pasar los casos del TCK donde lo no configurado queda como estaba, y sin los cambios al
   AST cambia el significado del programa (paréntesis) y los literales.
3. **Un árbol sin pérdida (CST).** Es el enfoque de Roslyn (C#) y de rust-analyzer (con `rowan`):
   un árbol que guarda cada token y cada espacio. Tiene fidelidad total, pero obliga a mantener un
   segundo árbol o a que el AST cargue espacios que el semántico, el intérprete y el linter no
   necesitan. rustfmt muestra que con el AST, las posiciones y el texto original alcanza. Queda como
   opción si el lenguaje suma comentarios y hace falta más fidelidad.
4. **Configuración como un objeto `Style` que lee el printer, como el `Config` de rustfmt.** Cada
   clave sería un campo y el printer preguntaría `style.spaceAfterColon()`. Elimina el orden entre
   reglas, pero agregar una regla obligaría a tocar el printer, y eso rompe lo que logró el ADR-026.
   Además, el «si no está configurada, queda como estaba» del TCK se expresa directo con reglas: si
   no hay regla para una brecha, sale el original.
5. **Un printer algebraico (el IR de documentos de Prettier, basado en Wadler).** Sirve para elegir
   cortes de línea según el ancho. rustfmt eligió reglas específicas por construcción, y PrintScript
   todavía no tiene ancho máximo. Si se agrega, `Shape` es el punto de partida, como en rustfmt.

## Consecuencias

**Lo que se gana**

- Las reglas hablan del lenguaje (declaración, operación, bloque, sentencia `println`) con el mismo
  vocabulario que las claves del TCK.
- Ninguna regla guarda estado ni repite la gramática: la estructura la da el parser, que es su único
  dueño.
- Una construcción nueva (`for`, menos unario, funciones) no compila hasta que el printer sepa
  escribirla, en vez de formatearse mal sin aviso.
- El formatting usa lo que produce el parser en vez de tirarlo. Se van el lexer con espacios, el
  token `WHITESPACE` y el puente `ParsedTokens`.
- Quedan habilitadas reglas que sobre tokens no se podían escribir: ancho máximo, paréntesis, alineación,
  límites de líneas vacías y reglas sobre `if`/`else`.
- El AST queda más fiel al código: los literales guardan su texto y los paréntesis existen.

**Lo que cuesta**

- Un nodo nuevo vuelve a tocar el formatter (ADR-009). Se acepta: el printer tiene que saber
  escribir cada nodo, igual que el intérprete tiene que saber ejecutarlo.
- Cambios en el AST y el parser: `GroupExpression` y literales con lexema. El nodo nuevo también
  obliga a tocar el semántico y el intérprete (`visitGroup`), y el linter tiene que mirar a través
  de los paréntesis para que `println((x))` siga siendo un argumento simple.
- Una pieza nueva, `SourceText` (módulo `source`), que tiene que liberar memoria a medida que
  avanza para no romper el streaming.
- Cada nodo tiene que declarar sus brechas, y el catálogo de `GapKind` crece con el lenguaje.
- Las reglas se reescriben, aunque las claves y el registro quedan iguales. Sus tests también, y
  los casos del TCK dan lo mismo (ahora corren en el build, ver Implementación).
- **Cambio incompatible en `formatter`:** `Formatter.format(Iterator<Token>, Writer)` pasa a
  `format(Iterator<Statement>, SourceText, Writer)`. El runner absorbe el cambio, así que la CLI y
  el TCK no se ven afectados.
- **Un cambio visible:** `mandatory-line-break-after-statement` ahora corta después de cualquier
  sentencia, también de un `if` (antes miraba solo el `;`).

## Implementación

1. **AST y parser:** `GroupExpression` (lo arma `GroupParselet`), `NumberLiteral` y
   `StringLiteral` con `getLexeme()`, y `visitGroup` en el semántico y el intérprete.
   `SimpleArgumentRule` mira a través de los paréntesis (`GroupExpression.unwrap`).
2. **`SourceText`** en el módulo `source`. El runner lo arma sobre el `Reader` del script y le pasa
   al lexer `source.reader()`, que graba lo que se lee. Lo que el cursor ya pasó se libera.
3. **Formatter:** `FormatterVisitor`, `FormattedOutput`, `Gap`, `GapKind` y `Shape`. Las reglas
   migradas mantienen sus claves en `FormattingRules`; `TokenSpacing` pasa a ser `Spacing`, que se
   aplica a ciertos `GapKind`.
4. **Runner:** `FormattingService` usa el mismo `ProgramLoader` que el análisis. Se borran las
   piezas del pipeline de tokens.
5. **Tests**, siguiendo a rustfmt. `FormatterGoldenTest` corre los 23 casos de formatter del TCK
   (los de 1.0 también en 1.1) y comprueba, en cada uno:
   - **Golden:** la salida es igual al resultado esperado.
   - **Idempotencia:** formatear el resultado esperado no lo cambia.
   - **Sin reglas, sin cambios:** con la configuración vacía, la salida es igual a la entrada.
   - **Mismo programa:** la entrada y la salida tienen los mismos tokens.
   - **Estilo por defecto:** también es idempotente.
6. **Memoria:** un script de 16 MB (453 440 sentencias) se formatea con 8 MB de heap en unos
   4 segundos, como pedía el ADR-023.

## Referencias

- rustfmt, *Design.md*: https://github.com/rust-lang/rustfmt/blob/master/Design.md
- rustfmt, trait `Rewrite` y `RewriteContext`: `src/rewrite.rs`
- rustfmt, `Shape` e `Indent`: `src/shape.rs`
- rustfmt, `format_missing`: `src/missed_spans.rs`
- rustfmt, `SpanUtils` (`span_after`, `span_before`): `src/source_map.rs`
- rustfmt, opciones de configuración: https://github.com/rust-lang/rustfmt/blob/master/Configurations.md
- `rustc_ast::ast::ExprKind::Paren` y `rustc_ast::token::Lit`: https://doc.rust-lang.org/nightly/nightly-rustc/rustc_ast/
