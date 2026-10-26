# ADR-020: `Program` se recorre una sola vez

- **Estado:** Aceptado
- **Fecha:** 2026-10-19
- **Relacionado:** reemplaza la parte de `toList()` de ADR-007 (Parser como `Iterator<Statement>` y `Program` lazy)

## Contexto

ADR-007 definió `Program` con dos métodos, `stream()` y `toList()`, y su javadoc prometía que "ambos producen el mismo resultado lógico". `LazyProgram` no lo cumplía: los dos métodos compartían el iterator del parser, así que si se recorría parte del programa con `stream()` y después se llamaba a `toList()`, la lista tenía sólo las sentencias restantes. Su propio javadoc lo advertía ("usar uno u otro, no ambos"). `EagerProgram` no tenía esa restricción.

O sea, las dos implementaciones no eran intercambiables (violación de Liskov): un consumidor correcto para `EagerProgram` podía dar resultados incompletos con `LazyProgram`, sin ningún error. Además, `toList()` invitaba a cargar el programa entero en memoria, que es lo que el diseño lazy busca evitar. El único uso en código productivo era `FormatterVisitor.visitProgram`.

## Decisión

- `Program` expone sólo `stream()`, y su contrato dice explícitamente que se recorre **una sola vez**. Es lo que puede garantizar un programa que se parsea a medida que se lee.
- `LazyProgram` deja de cachear: un segundo `stream()` lanza `IllegalStateException` en lugar de devolver silenciosamente lo que quedaba.
- `EagerProgram` puede recorrerse varias veces, pero eso es un detalle suyo: el contrato de `Program` no lo promete.
- `FormatterVisitor.visitProgram` recorre con `stream()`. Los tests que necesitan una lista la arman explícitamente.

## Consecuencias

- Cualquier consumidor escrito contra el contrato de `Program` funciona igual con las dos implementaciones.
- Usar mal un `LazyProgram` (recorrerlo dos veces) falla en el momento, en vez de producir un resultado incompleto.
- Quien necesite varias pasadas tiene que guardarse las sentencias a propósito, y esa decisión de memoria queda visible en su código.
- El adapter del TCK sólo usa `stream()`, así que no se ve afectado.
