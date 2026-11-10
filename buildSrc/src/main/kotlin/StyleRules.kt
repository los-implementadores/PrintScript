/**
 * Una regla de estilo propia del proyecto: un patrón que no puede aparecer en el código y cómo se
 * arregla.
 *
 * Spotless reemplaza [pattern] por [replacement] y Checkstyle falla si encuentra [pattern]. Como los
 * dos usan el mismo patrón, lo que deja `spotlessApply` siempre pasa Checkstyle: no se pueden
 * contradecir.
 */
data class StyleRule(
    val id: String,
    val message: String,
    val pattern: String,
    val replacement: String,
)

/**
 * Las reglas de estilo propias, en un solo lugar. Las usan Spotless (para arreglar) y Checkstyle
 * (para verificar), ver printscript.java-conventions.gradle.kts.
 *
 * Agregar una regla es sumarla a esta lista y declarar su módulo `RegexpMultiline` en
 * config/checkstyle/checkstyle.xml (el build avisa si falta). El patrón tiene que dejar de
 * encontrarse después del reemplazo, y tiene que ser algo que google-java-format no vuelva a poner.
 */
object StyleRules {
    val all =
        listOf(
            StyleRule(
                id = "lineCommentSpace",
                message = "Un comentario // lleva un espacio después de las barras.",
                pattern = """(?m)^([ \t]*)//(?=[^\s/])""",
                replacement = "$1// ",
            ),
            StyleRule(
                id = "noAuthorTag",
                message = "Sin @author en el Javadoc: la autoría la registra git.",
                pattern = """(?m)^[ \t]*\*[ \t]*@author\b.*\R""",
                replacement = "",
            ),
            StyleRule(
                id = "preferIsEmpty",
                message = "Usar isEmpty() en vez de size() == 0, length() == 0 o equals(\"\").",
                pattern = """\.(?:size\(\)[ \t]*==[ \t]*0|length\(\)[ \t]*==[ \t]*0|equals\(""\))(?![\w.])""",
                replacement = ".isEmpty()",
            ),
            StyleRule(
                id = "noTrueComparison",
                message = "Un booleano no se compara con un literal: x en vez de x == true o x != false.",
                pattern = """[ \t]*(?:==[ \t]*true|!=[ \t]*false)\b""",
                replacement = "",
            ),
            StyleRule(
                id = "noBlankLineBeforeClosingBrace",
                message = "Sin línea vacía antes de la llave que cierra un bloque.",
                pattern = """(?m)^[ \t]*\R(?=[ \t]*\})""",
                replacement = "",
            ),
        )
}
