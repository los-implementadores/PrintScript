# ADR-017: Excepciones tipadas para los errores del lenguaje

- **Estado:** Aceptado
- **Fecha:** 2026-10-19
- **Relacionado:** ADR-006 (Fail-fast con `ParseException`), ADR-016 (Separar la CLI en capas)

## Contexto

El lexer y el parser ya tenían `LexerException` y `ParseException` con su `Position`, pero el análisis semántico y el intérprete lanzaban `RuntimeException` con el texto armado a mano (`"Semantic Error at " + pos + ": ..."`), repetido en cada `throw`. Quien orquesta (la CLI, una futura API) no podía distinguir un error del programa del usuario de un bug de la implementación (un `NullPointerException`, por ejemplo) sin parsear el mensaje, y tampoco podía obtener la posición del error como dato.

## Decisión

- Se agrega `PrintScriptException` en `common`: base abstracta de todos los errores del código fuente, con `getPosition()`.
- `LexerException` y `ParseException` pasan a extenderla, sin cambiar sus mensajes.
- Se agregan `SemanticException` (paquete `interpreter.semantic`) y `ExecutionException` (paquete `interpreter`). Reciben el detalle y la posición; el prefijo `"Semantic Error at <pos>: "` / `"Execution Error at <pos>: "` lo arma el constructor, una sola vez.
- `SemanticAnalyzerVisitor`, `InterpreterVisitor` y `TypeCoercion` lanzan estos tipos. Los mensajes resultantes son idénticos a los anteriores, así que la salida de la CLI y del TCK no cambia.

## Consecuencias

- Un consumidor puede hacer `catch (PrintScriptException e)` para reportar errores del usuario con su posición y dejar que cualquier otra excepción se trate como falla interna (por ejemplo, 400 vs 500 en una API).
- Los tests pueden afirmar el tipo de error (`assertThrows(SemanticException.class, ...)`) además del texto.
- Los errores de `Environment` (`common`) siguen siendo `RuntimeException` sin posición. Sólo se alcanzan si no corrió el análisis semántico antes, o por la falta de scopes de bloque (pendiente aparte).
