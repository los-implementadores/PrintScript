# ADR-027: El formatter y el linter no usan el visitor

- **Estado:** Aceptado
- **Fecha:** 2026-11-10
- **Relacionado:** ADR-002 (Visitor), ADR-009 (limitación OCP del visitor), ADR-012 (formateador streaming, reemplazado por este), ADR-015 (reglas del linter), ADR-026 (registro de reglas)

## Contexto

ADR-009 aceptó el costo del visitor: cada nodo nuevo obliga a agregar su `visitX` en todas las
implementaciones (el *expression problem*), a cambio de poder sumar operaciones sin tocar los
nodos. El argumento era que en un compilador crecen más las operaciones que los nodos.

Para el semántico y el intérprete el argumento vale: un nodo nuevo necesita su chequeo de tipos y
su ejecución, así que tocarlos es parte del trabajo. Para el formatter y el linter no:

- Lo que más va a crecer en PrintScript son las construcciones del lenguaje (una versión nueva
  trae nodos nuevos), no las herramientas. Con el visitor, cada nodo nuevo obligaba a tocar el
  formatter y el linter aunque ninguna regla lo mirara.
- El formatter tenía dos implementaciones con reglas distintas: un visitor que regeneraba todo el
  código desde el AST (y perdía los espacios que ninguna regla pedía cambiar) y un formateador de
  texto con expresiones regulares línea por línea (el que usaba el TCK, ADR-012). La CLI usaba uno y
  el TCK el otro.

## Decisión

**Formatter: trabaja sobre tokens.** El lexer puede devolver los espacios como tokens
(`LexerFactory.createWithWhitespace`), y el lexema de un string trae sus comillas, así que los
tokens reconstruyen el código fuente exacto. `FormatterImpl` reescribe cada token tal cual y le
pasa a las reglas cada espacio entre dos tokens (`Gap`). Cada `FormattingRule` decide solo ese
espacio, o lo deja como está. Se borran `FormatterVisitor` y `StreamingFormatter`: queda un solo
formateador, para la CLI y para el TCK.

**Linter: recorrido genérico.** `Node` tiene `children()` (las expresiones, sentencias y bloques que
contiene). `StaticAnalyzerImpl` baja por los hijos y le avisa a cada regla cada nodo al entrar y al
salir (`onEnter`, `onExit`); cada regla mira los tipos que le interesan e ignora el resto. Se borra
`AnalyzerVisitor`. Los bloques pasan a ser un nodo, `Block`, así cualquier construcción con cuerpo
abre un scope que `UnusedVariableRule` ya sabe manejar.

`Program` deja de ser un nodo (no tiene hijos que se puedan pedir sin consumir el parser) y
`ASTVisitor` pierde `visitProgram` y suma `visitBlock`.

## Consecuencias

- Un nodo nuevo no toca el formatter (no hay nodos ahí) ni el linter (solo tiene que implementar
  `children()`). Sí toca al semántico y al intérprete, como corresponde.
- Sin reglas, el formatter devuelve el código idéntico. Una regla que no está en la configuración
  no cambia nada (lo que pide el TCK), y la CLI sin configuración usa un estilo por defecto
  (`FormattingRules.defaults()`) con las reglas de la consigna.
- El formatter sigue en memoria constante y sigue validando la sintaxis: el runner le pasa los
  tokens a medida que el parser lee cada sentencia (`ParsedTokens`), así un error de sintaxis
  frena el formateo (ADR-023).
- Cambios visibles en la salida del formatter de la CLI: ya no agrega un salto de línea al final
  si el archivo no lo tenía, y los números se escriben como están (antes `5.0` salía `5`). Las
  dos claves de `println` quedaron separadas: `newlineBeforePrintln` (antes) y
  `line-breaks-after-println` (después, como el TCK).
- **Cambio incompatible:** `Formatter` pasa a ser `format(Iterator<Token>, Writer)`; se van
  `format(Program)`, `format(Reader, Writer)`, `format(String)` y `formatIterator`. `LintRule` pasa
  a `onEnter`/`onExit`/`onFinish`. El adapter del formatter del TCK tiene que usar el runner.
