# ADR-012: Formateador en streaming/iterable sobre código fuente y reglas aisladas

- **Estado:** Aceptado
- **Fecha:** 2026-09-19
- **Relacionado:** ADR-002 (Visitor para recorrer el AST), ADR-007 (Parser como `Iterator<Statement>` y `Program` como interfaz lazy)

## Contexto

El formateador de PrintScript se diseñó originalmente como un pretty-printer canónico basado en AST (ADR-002): la interfaz `Formatter` exponía `String format(Program program)`, delegando en un `FormatterVisitor` que recorría el árbol sintáctico abstracto e imprimía una versión completamente estandarizada del código de acuerdo a las `FormattingRules`.

Al integrar PrintScript con suites de conformidad externas (como el TCK de la cátedra) y al formatear archivos reales en herramientas de desarrollo, se identificaron dos limitaciones arquitectónicas fundamentales de este enfoque:

1. **Pérdida de trivia en el AST:** Un AST descarta durante el análisis léxico los espacios en blanco, tabulaciones y saltos de línea originales. Si una configuración activa únicamente una regla aislada (por ejemplo, *espacio después de los dos puntos*), el visitor de AST se ve obligado a "inventar" el formato de todo lo demás (espacios alrededor del `=`, operadores, saltos de línea) usando valores por defecto. Si el archivo original tenía formateo mixto o inconsistencias deliberadas que no debían tocarse, el visitor las destruye al regenerar todo desde cero.
2. **Consumo de memoria en archivos grandes:** Cargar todo el código fuente en memoria como un único `String` (o construir el AST completo de archivos gigantes) contradice la filosofía lazy adoptada en ADR-007, donde el `Lexer` es un `Iterator<Token>` y el `Parser` consume sentencias bajo demanda. En entornos con restricciones severas de memoria (como el test de archivos grandes del TCK que corre con `maxHeapSize = 7m`), un formateador monolítico produce `OutOfMemoryError`.

## Decisión

Se decidió extender el módulo `formatter` para soportar **dos estrategias complementarias** bajo la misma interfaz `Formatter`:

1. **Estrategia 1 (AST Pretty-Printer):** Se conserva `format(Program program)` delegando en `FormatterVisitor`. Mantiene intacta la retrocompatibilidad con los tests unitarios existentes y es la opción recomendada cuando se desea una normalización agresiva total o regenerar código tras manipulaciones sintácticas.
2. **Estrategia 2 (Streaming / Rule-based Formatter):** Se agregan a la interfaz:
   - `void format(Reader reader, Writer writer)`
   - `String format(String source)`
   - `Iterator<String> formatIterator(Reader reader)`

### Implementación: `StreamingFormatter` como `Iterator<String>`

La clase `StreamingFormatter` implementa `Iterator<String>` procesando el código fuente bajo demanda desde un `Reader` con las siguientes características:

- **Uso de `BufferedReader`:** Se decora el `Reader` de entrada con un `BufferedReader` para leer eficientemente línea por línea mediante `readLine()`, reteniendo en RAM únicamente la línea activa.
- **Uso de `Deque<String>` como buffer FIFO:**
  - *Problema de cardinalidad $1 \to N$:* Ciertas reglas pueden producir múltiples líneas a partir de una sola (por ejemplo, `mandatory-line-break-after-statement` divide sentencias concatenadas con `;`, `if-brace-below-line` separa la llave `{` en una línea nueva, o `line-breaks-after-println` añade líneas en blanco).
  - *FIFO vs. LIFO:* Para que el `Iterator` pueda entregar un elemento por vez sin invertir el orden del código fuente, se requiere una cola FIFO (*First In, First Out*). Se utiliza `ArrayDeque` (`buffer.add()` para encolar al final y `buffer.pollFirst()` para desencolar por el frente).
  - *Rechazo de `java.util.Stack`:* Se descarta `Stack` porque es una clase obsoleta (legacy desde Java 1.0) que implementa LIFO (lo que invertiría las líneas del programa) y sincroniza innecesariamente todos sus métodos con bloqueos pesados.
- **Pipeline de reglas aisladas:** Las transformaciones se ejecutan de forma quirúrgica:
  - División de sentencias concatenadas tras `;`.
  - Posicionamiento de llaves `{` en `if` (misma línea o línea separada).
  - Cálculo de indentación según profundidad de llaves anidadas.
  - Normalización de espacios en `=`, `:` y operadores fuera de literales de cadena (`"..."`).
  - Inserción de líneas en blanco tras `println`.
- **Memoria constante $O(1)$:** En ningún momento residen más de 1 o 2 líneas en el heap, permitiendo formatear streams de cualquier tamaño dentro del límite de 7MB.

### Cambios en `FormattingRules`

1. **Soporte de claves TCK y camelCase:** Se anotaron los campos con `@SerializedName` de Gson utilizando alias en `kebab-case` (`enforce-spacing-after-colon-in-declaration`, `enforce-spacing-around-equals`, `indent-inside-if`, etc.).
2. **Separación de contratos:**
   - Para la estrategia AST, se conservan getters con defaults razonables (`isSpaceAfterColonAst()`, `getIndentSize()`).
   - Para la estrategia Streaming, se implementaron getters booleanos directos (`isSpaceAfterColon()`, `isSpaceAroundAssign()`, `isLineBreakAfterStatement()`, `hasIndentSize()`) que evalúan si la regla fue activada explícitamente (`!= null && value`), eliminando el uso de `Boolean.TRUE.equals(...)` y permitiendo que las reglas no configuradas permanezcan inactivas sin sobreescribir el código fuente.

## Consecuencias

- **Cero regresiones:** Todos los tests preexistentes del visitor de AST continúan pasando sin modificaciones.
- **Conformidad 100% con el TCK:** El adaptador en `printscript-tck` (`PrintScriptFormatterAdapter`) se reduce a un puente limpio de pocas líneas que delega directamente en `FormatterImpl` de Maven, resolviendo los 23 tests de formateo.
- **Consistencia de diseño:** `Lexer`, `Parser` y `Formatter` comparten ahora el mismo paradigma de procesamiento perezoso/iterable definido en ADR-007.
