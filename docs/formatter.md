# Formatter — Formateador de código

El formatter recibe los tokens del código fuente, espacios incluidos, y reescribe el código
aplicando las reglas que activa un archivo de configuración (JSON). Vive en el módulo `formatter`,
que depende solo de `token` y de `rules`: no usa el AST ni el parser, y no sabe de versiones del
lenguaje.

---

## Cómo funciona

El código fuente es una secuencia de tokens con espacio entre ellos:

```
let␣x:number␣=␣5;⏎println(x);
```

El formatter reescribe cada token **tal como está** y lo único que cambia es el espacio entre dos
tokens (un `Gap`). Cada regla mira un `Gap` y devuelve el espacio que tiene que quedar; si ese
espacio no le importa, lo devuelve sin cambios. Por eso:

- **Sin reglas, la salida es idéntica a la entrada**, tabs, comillas y líneas vacías incluidas.
- **Una regla que no está en la configuración no toca el código.** Si solo se activa "espacio
  después del `:`", el resto queda como estaba (es lo que pide el TCK).
- **Un nodo nuevo en el lenguaje no cambia el formatter**: no hay un método por nodo (ADR-027).
- **Memoria constante**: se lee un token por vez y se escribe enseguida.

Los tokens con espacios salen de `LexerFactory.createWithWhitespace(reader)`: el mismo lexer, con
un matcher más que devuelve los espacios como tokens `WHITESPACE`. El parser nunca los ve.

---

## Archivos y su responsabilidad

| Paquete | Archivo | Responsabilidad |
| --- | --- | --- |
| `org.printscript.formatter` | `Formatter` | Interfaz: `format(Iterator<Token>, Writer)` |
| | `FormatterImpl` | Reescribe los tokens y le pasa cada `Gap` a las reglas, en orden. No conoce ninguna regla |
| | `Gap` | El espacio entre dos tokens: el token de antes, el de después, el espacio y la indentación de la línea |
| | `FormattingRule` | Interfaz de una regla: `String apply(Gap gap)` |
| `org.printscript.formatter.rule` | `FormattingRules` | **El único lugar donde se registran las reglas**, con su clave de configuración |
| | `TokenSpacing`, `LineBreakAfterStatement`, `BracePlacement`, ... | Una clase por regla |

La lectura del JSON y el mecanismo "clave → regla" (`RuleConfig`, `RuleRegistry`) están en el
módulo `rules`, que comparte con el linter (ADR-026).

---

## Reglas configurables

Las claves en camelCase son las propias; las otras son las del TCK. Con una clave propia, `false`
pide lo contrario (`"spaceAroundAssign": false` deja el `=` sin espacios); con una del TCK,
`false` deja la regla apagada. Las claves desconocidas se ignoran, y un valor del tipo equivocado
(`"indentSize": "cuatro"`) es un error.

| Regla | Clave propia | Clave del TCK | Valor |
| --- | --- | --- | --- |
| Salto de línea después de cada `;` | — | `mandatory-line-break-after-statement` | boolean |
| Llave del `if` en la misma línea | — | `if-brace-same-line` | boolean |
| Llave del `if` en la línea de abajo | — | `if-brace-below-line` | boolean |
| Líneas vacías antes de un `println` | `newlineBeforePrintln` | — | 0, 1 o 2 |
| Líneas vacías después de un `println` | — | `line-breaks-after-println` | número |
| Como mucho un espacio entre tokens | `maxOneSpaceBetweenTokens` | — | boolean |
| Exactamente un espacio entre tokens | — | `mandatory-single-space-separation` | boolean |
| Espacio antes del `:` | `spaceBeforeColon` | `enforce-spacing-before-colon-in-declaration` | boolean |
| Espacio después del `:` | `spaceAfterColon` | `enforce-spacing-after-colon-in-declaration` | boolean |
| Espacios alrededor del `=` | `spaceAroundAssign` | `enforce-spacing-around-equals` / `enforce-no-spacing-around-equals` | boolean |
| Espacios alrededor de los operadores | `spaceAroundOperators` | `mandatory-space-surrounding-operations` | boolean |
| Indentación dentro de los bloques | `indentSize` | `indent-inside-if` | espacios por nivel |

Las reglas se aplican en ese orden: primero las que deciden dónde se corta cada línea, después
los espacios dentro de una línea y al final la indentación, que corrige el comienzo de todas las
líneas (también las que cortaron las reglas anteriores).

### Estilo por defecto

Si la CLI no recibe configuración, usa `FormattingRules.defaults()`: las reglas de la consigna
(salto de línea después de cada `;`, como mucho un espacio entre tokens, espacio alrededor de los
operadores), espacio después del `:` y alrededor del `=`, una línea vacía antes de cada `println`
e indentación de 4 espacios. Es lo mismo que [`formatter_rules.json`](../formatter_rules.json):

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
let name:string="Joe";
let lastName:string ="Doe";
println(name +" "+ lastName);
```

```
let name: string = "Joe";
let lastName: string = "Doe";

println(name + " " + lastName);
```

---

## Cómo agregar una regla

1. Escribir una clase que implemente `FormattingRule` en `org.printscript.formatter.rule`. Recibe
   cada `Gap` del archivo en orden, así que puede llevar estado (como `BlockIndentation`, que
   cuenta las llaves abiertas). Si el `Gap` no le importa, devuelve `gap.whitespace()`.
2. Registrarla con su clave en `FormattingRules`, en el lugar que le toca según el orden de arriba:

```java
.rule("spaceAfterComma").withBoolean(on -> TokenSpacing.after(TokenType.COMMA, on))
```

Con eso ya se activa desde el JSON. Ni `FormatterImpl` ni la lectura de la configuración cambian.
Una regla que no viene de la configuración se le puede pasar directo:
`new FormatterImpl(() -> List.of(new MiRegla()))` (es una fábrica porque las reglas guardan
estado y cada formateo arranca con reglas nuevas).

---

## Uso típico

Lo normal es usarlo a través del runner, que además valida la sintaxis con la gramática de la
versión (un error de sintaxis frena el formateo):

```java
LanguageServices.printScript()
    .formatting()
    .format(reader, LanguageVersion.V1_1, RuleConfig.fromJson(configReader), writer);
```

Directo, sin validar:

```java
Formatter formatter = new FormatterImpl(RuleConfig.fromJson(configReader));
formatter.format(LexerFactory.createWithWhitespace(reader), writer);
```

---

## Cómo se conecta con el resto

```
Reader ──► Lexer con espacios ──► ParsedTokens (runner) ──► FormatterImpl ──► Writer
                                       │
                                       └─► Parser (solo valida, sin espacios)
```

`ParsedTokens` le pasa al formatter todos los tokens y al parser los mismos sin espacios. Cada
vez que el formatter se queda sin tokens, el parser lee la sentencia siguiente: nunca hay más de
una sentencia en memoria.
