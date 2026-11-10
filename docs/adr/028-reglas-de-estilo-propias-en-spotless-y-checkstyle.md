# ADR-028: Spotless y Checkstyle comparten 5 reglas de estilo propias

- **Estado:** Aceptado
- **Fecha:** 2026-11-10

## Contexto

El código Java del proyecto se formatea con Spotless (google-java-format) y se verifica con
Checkstyle. Los dos se configuraban por separado y ya chocaron una vez: Checkstyle pedía 4
espacios de indentación y google-java-format pone 2, así que todo el código formateado daba
warnings. Además, las reglas de los dos eran presets ajenos; no había ninguna regla propia del
proyecto.

## Decisión

Se agregan 5 reglas propias, iguales en los dos, definidas **una sola vez** en
`buildSrc/src/main/kotlin/StyleRules.kt`. Cada regla es un patrón que no puede aparecer en el
código y su arreglo:

| Regla | Qué no puede aparecer | Arreglo |
| --- | --- | --- |
| `lineCommentSpace` | `//comentario` | `// comentario` |
| `noAuthorTag` | `@author` en el Javadoc (la autoría la registra git) | se borra la línea |
| `preferIsEmpty` | `size() == 0`, `length() == 0`, `equals("")` | `isEmpty()` |
| `noTrueComparison` | `x == true`, `x != false` | `x` |
| `noBlankLineBeforeClosingBrace` | una línea vacía antes de `}` | se borra la línea |

- **Spotless** reemplaza cada patrón por su arreglo (`replaceRegex`), antes de google-java-format.
- **Checkstyle** falla si encuentra el patrón: un módulo `RegexpMultiline` por regla, que lee el
  patrón y el mensaje de `StyleRules` (por `configProperties`).

Como los dos usan el mismo patrón, lo que deja `spotlessApply` siempre pasa Checkstyle. Si una regla
de `StyleRules` no tiene su módulo en `checkstyle.xml`, el build falla al configurarse.

## Consecuencias

- El código actual ya cumplía las 5 reglas: agregarlas no cambió ningún archivo.
- Agregar una regla es sumarla a `StyleRules` y declarar su módulo en `checkstyle.xml`. El patrón
  tiene que dejar de encontrarse después del arreglo, y no puede ser algo que google-java-format
  vuelva a poner.
- Las reglas son expresiones regulares sobre el texto: no distinguen código de strings o
  comentarios. Se eligieron para que eso no importe en la práctica.
- Un plugin de Checkstyle en el IDE que lea `checkstyle.xml` directo necesita que le pasen esas
  propiedades; el build de Gradle es la referencia.
