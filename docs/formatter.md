# Formatter — Formateador de código

El formatter recorre el AST y reescribe el código aplicando las reglas que activa un archivo de
configuración (JSON). Vive en el módulo `formatter`, que depende de `ast`, `source` y `rules`: no
usa el lexer ni el parser, y no sabe de versiones del lenguaje. El diseño sigue al de rustfmt, el
formatter de Rust (ADR-029).

---

## Cómo funciona

Cada nodo del AST sabe qué partes tiene. Una declaración, por ejemplo:

```
let␣x␣:␣number␣=␣5␣;
```

Las partes (`let`, `x`, `:`, `number`, `=`, `5`, `;`) salen del AST. Entre cada par hay una
**brecha** (`Gap`) y cada brecha dice **qué espacio es** en términos del lenguaje
(`AFTER_KEYWORD`, `BEFORE_TYPE_COLON`, `AFTER_TYPE_COLON`, `BEFORE_ASSIGN`…). El espacio de cada
brecha arranca con el que había en el código original, y cada regla mira la brecha y devuelve el
espacio que tiene que quedar. Si la brecha no le importa, la devuelve sin cambios. Por eso:

- **Sin reglas, la salida es idéntica a la entrada**, tabs, comillas y líneas vacías incluidas.
- **Una regla que no está en la configuración no toca el código.** Si solo se activa "espacio
  después del `:`", el resto queda como estaba (es lo que pide el TCK).
- **Las reglas hablan del lenguaje, no de símbolos.** «Espacio antes del `:` de una declaración»
  es la brecha `BEFORE_TYPE_COLON`; «la sentencia anterior es un `println`» es
  `gap.previous()`; la profundidad de bloques es `gap.shape().depth()`. Ninguna regla guarda
  estado ni reconstruye la gramática.
- **El programa no cambia.** Los paréntesis son un nodo (`GroupExpression`) y los literales
  guardan su texto (`getLexeme()`): `(1 + 2) * 3` y `'hola'` salen como estaban.

Los espacios no están en el AST: salen del código original. `SourceText` (módulo `source`) guarda
lo que lee el lexer y el formatter lo recorre con un cursor, en paralelo con el AST, como el
`last_pos` de rustfmt. Si el AST y el código no coinciden, el formatter falla en vez de escribir
otro programa.

---

## Archivos y su responsabilidad

| Paquete | Archivo | Responsabilidad |
| --- | --- | --- |
| `org.printscript.formatter` | `Formatter` | Interfaz: `format(Iterator<Statement>, SourceText, Writer)` |
| | `FormatterImpl` | Arma las reglas y escribe el programa con `FormatterVisitor`. No conoce ninguna regla |
| | `FormatterVisitor` | Un método por nodo: qué partes tiene y qué brecha hay entre cada par. Es el `Rewrite` de rustfmt |
| | `FormattedOutput` | Escribe las partes y las brechas: lleva el cursor del código original, la profundidad y la indentación de la línea |
| | `Gap` | Una brecha: qué es (`GapKind`), de qué nodo, la sentencia anterior, el espacio y el `Shape` |
| | `GapKind` | Los tipos de brecha del lenguaje |
| | `Shape` | Dónde está la brecha: profundidad de bloques e indentación de la línea |
| | `FormattingRule` | Interfaz de una regla: `String apply(Gap gap)` |
| `org.printscript.formatter.rule` | `FormattingRules` | **El único lugar donde se registran las reglas**, con su clave de configuración |
| | `Spacing`, `LineBreakAfterStatement`, `BracePlacement`, ... | Una clase por regla |
| `org.printscript.source` (módulo `source`) | `SourceText` | El código original: lo graba mientras lo lee el lexer y lo recorre con un cursor |

La lectura del JSON y el mecanismo "clave → regla" (`RuleConfig`, `RuleRegistry`) están en el
módulo `rules`, que comparte con el linter (ADR-026).

### Las brechas

| `GapKind` | Dónde está |
| --- | --- |
| `FILE_START` / `FILE_END` | Antes de la primera sentencia / después de la última |
| `BETWEEN_STATEMENTS` | Entre dos sentencias, del archivo o de un bloque |
| `BLOCK_START` / `BLOCK_END` | Después de la `{` / antes de la `}` de un bloque |
| `AFTER_KEYWORD` | `let␣x`, `const␣x`, `if␣(` |
| `BEFORE_TYPE_COLON` / `AFTER_TYPE_COLON` | `x␣:` / `:␣number` en una declaración |
| `BEFORE_ASSIGN` / `AFTER_ASSIGN` | `x␣=` / `=␣5` en una declaración o asignación |
| `BEFORE_OPERATOR` / `AFTER_OPERATOR` | `a␣+` / `+␣b` |
| `BEFORE_SEMICOLON` | `x␣;` |
| `BEFORE_ARGUMENTS` | `println␣(` |
| `INSIDE_PARENS` | `(␣x␣)`, en llamadas, agrupaciones y condiciones |
| `BEFORE_COMMA` / `AFTER_COMMA` | Entre argumentos |
| `BEFORE_BLOCK` | `)␣{` del `if` y `else␣{` |
| `BEFORE_ELSE` | `}␣else` |

---

## Reglas configurables

Las claves en camelCase son las propias; las otras son las del TCK. Con una clave propia, `false`
pide lo contrario (`"spaceAroundAssign": false` deja el `=` sin espacios); con una del TCK,
`false` deja la regla apagada. Las claves desconocidas se ignoran, y un valor del tipo equivocado
(`"indentSize": "cuatro"`) es un error.

| Regla | Clave propia | Clave del TCK | Valor | Brechas |
| --- | --- | --- | --- | --- |
| Salto de línea después de cada sentencia | — | `mandatory-line-break-after-statement` | boolean | `BETWEEN_STATEMENTS`, `BLOCK_END` |
| Llave del `if` en la misma línea | — | `if-brace-same-line` | boolean | `BEFORE_BLOCK` |
| Llave del `if` en la línea de abajo | — | `if-brace-below-line` | boolean | `BEFORE_BLOCK` |
| Líneas vacías antes de un `println` | `newlineBeforePrintln` | — | 0, 1 o 2 | `BETWEEN_STATEMENTS` |
| Líneas vacías después de un `println` | — | `line-breaks-after-println` | número | `BETWEEN_STATEMENTS` |
| Como mucho un espacio en una línea | `maxOneSpaceBetweenTokens` | — | boolean | todas las de una línea |
| Exactamente un espacio en una línea | — | `mandatory-single-space-separation` | boolean | todas las de una línea |
| Espacio antes del `:` | `spaceBeforeColon` | `enforce-spacing-before-colon-in-declaration` | boolean | `BEFORE_TYPE_COLON` |
| Espacio después del `:` | `spaceAfterColon` | `enforce-spacing-after-colon-in-declaration` | boolean | `AFTER_TYPE_COLON` |
| Espacios alrededor del `=` | `spaceAroundAssign` | `enforce-spacing-around-equals` / `enforce-no-spacing-around-equals` | boolean | `BEFORE_ASSIGN`, `AFTER_ASSIGN` |
| Espacios alrededor de los operadores | `spaceAroundOperators` | `mandatory-space-surrounding-operations` | boolean | `BEFORE_OPERATOR`, `AFTER_OPERATOR` |
| Indentación dentro de los bloques | `indentSize` | `indent-inside-if` | espacios por nivel | las que empiezan una línea |

Las reglas se aplican en ese orden: primero las que deciden dónde se corta cada línea, después
los espacios dentro de una línea y al final la indentación, que corrige el comienzo de todas las
líneas (también las que cortaron las reglas anteriores).

### Estilo por defecto

Si la CLI no recibe configuración, usa `FormattingRules.defaults()`: las reglas de la consigna
(salto de línea después de cada sentencia, como mucho un espacio en una línea, espacio alrededor
de los operadores), espacio después del `:` y alrededor del `=`, una línea vacía antes de cada
`println` e indentación de 4 espacios. Es lo mismo que
[`formatter_rules.json`](../formatter_rules.json):

```json
{
  "mandatory-line-break-after-statement": true,
  "maxOneSpaceBetweenTokens": true,
  "spaceBeforeColon": false,
  "spaceAfterColon": true,
  "spaceAroundAssign": true,
  "spaceAroundOperators": true,
  "newlineBeforePrintln": 1,
  "indentSize": 4
}
```

---

## Ejemplo de entrada → salida

Con el estilo por defecto:

```
let name:string='Joe';
let total:number=(1+2)*3.0;
println(name +" "+ total);
```

```
let name: string = 'Joe';
let total: number = (1 + 2) * 3.0;

println(name + " " + total);
```

---

## Cómo agregar una regla

1. Escribir una clase que implemente `FormattingRule` en `org.printscript.formatter.rule`. Mira
   `gap.kind()` (y si hace falta `gap.node()`, `gap.previous()` o `gap.shape()`) y, si la brecha
   no le importa, devuelve `gap.whitespace()`.
2. Registrarla con su clave en `FormattingRules`, en el lugar que le toca según el orden de arriba:

```java
.rule("spaceAfterComma").withBoolean(on -> Spacing.at(on, GapKind.AFTER_COMMA))
```

Con eso ya se activa desde el JSON. Ni `FormatterImpl` ni la lectura de la configuración cambian.
Una regla que no viene de la configuración se le puede pasar directo:
`new FormatterImpl(() -> List.of(new MiRegla()))` (es una fábrica para que cada formateo arranque
con reglas nuevas, por si una guarda estado).

## Cómo agregar un nodo

Un nodo nuevo del AST no compila hasta que `FormatterVisitor` tenga su `visitX`: ahí se escriben
sus partes en orden y una brecha entre cada par. Si ninguna de las de `GapKind` le sirve, se agrega
una. El compilador marca el lugar; no hay forma de que un nodo nuevo se formatee mal sin aviso.

---

## Uso típico

Lo normal es usarlo a través del runner, que parsea con la gramática de la versión (un error de
sintaxis frena el formateo):

```java
LanguageServices.printScript()
    .formatting()
    .format(reader, LanguageVersion.V1_1, RuleConfig.fromJson(configReader), writer);
```

Directo, con un parser propio:

```java
SourceText source = SourceText.recording(reader);
Iterator<Statement> statements = new ParserImpl(LexerFactory.create(source.reader()), grammar);
new FormatterImpl(RuleConfig.fromJson(configReader)).format(statements, source, writer);
```

---

## Cómo se conecta con el resto

```
Reader ──► SourceText (graba) ──► Lexer ──► Parser ──► Statement ──► FormatterVisitor ──► Writer
               │                                                         ▲
               └──────────── espacios originales (cursor) ───────────────┘
```

El runner arma el formatting igual que el análisis (`LazyProgramLoader`), y además envuelve el
`Reader` en un `SourceText`. El parser produce una sentencia por vez y el formatter la escribe
antes de pedir la siguiente; `SourceText` libera lo que el cursor ya pasó. Un script de 16 MB se
formatea con 8 MB de heap.

## Tests

- `FormatterTest` y `FormattingRulesTest` (módulo `formatter`): el motor y cada regla.
- `FormatterGoldenTest` (módulo `runner`): los casos de formatter del TCK (copiados del fork) y, como
  en rustfmt, que formatear la salida no la cambia, que sin reglas no cambia nada, que solo cambian
  los espacios y que el estilo por defecto es idempotente.
