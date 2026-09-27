# ADR-013: Separar la CLI en I/O, comandos y orquestación del lenguaje

- **Estado:** Aceptado
- **Fecha:** 2026-09-27
- **Relacionado:** ADR-007 (Parser como `Iterator<Statement>` y `Program` lazy), ADR-012 (Formateador en streaming)

## Contexto

`cli/Main.java` concentraba todo en un solo método: parseo de argumentos, menú interactivo, configuración de los `TokenMatcher` del lexer, armado del pipeline lexer → parser → semántico → intérprete/linter/formatter, lectura y escritura de archivos y `System.out.println` para informar al usuario. Además, el intérprete imprimía `println` y los prompts de `readInput` directo a `System.out`, y `AnalyzerConfig.fromJson` escribía en `System.err` y seguía con defaults ante un JSON inválido.

A futuro se quiere exponer las mismas operaciones por una API HTTP (un endpoint para ejecutar, otro para formatear, otro para lint, etc.). Con el diseño anterior eso implicaba duplicar la orquestación o capturar `System.out`.

## Decisión

Se separa en tres capas:

1. **Orquestación del lenguaje — módulo `runner` (puro).** Un servicio por operación: `ValidationService`, `AnalysisService`, `ExecutionService`, `FormattingService`. Reciben el código como `Reader` más `LanguageVersion` y la config ya parseada, y devuelven resultados (`List<Violation>`, `String` formateado) o lanzan excepción. No tocan consola ni filesystem. `LexerFactory` y `ProgramLoader` centralizan el armado del pipeline.
2. **I/O impuro — `cli.io`.** `Console` (única vía a stdout/stderr/stdin) y `FileAccess` (leer/escribir archivos), con sus implementaciones `SystemConsole` y `LocalFileAccess`.
3. **Command pattern — `cli.command`.** Cada `Command` adapta un servicio del `runner` a la terminal (abre archivos, invoca, imprime). `CommandRegistry` los indexa por nombre; el `InteractiveMenu` se construye a partir del registry. `CliApplication` hace el wiring y devuelve un exit code; `Main` sólo lo conecta a `System.*`.

Para que la ejecución sea pura, el intérprete recibe un `OutputProvider` inyectable (análogo a `InputProvider`/`EnvProvider`). Se proveen `StdoutOutputProvider` (default, retrocompatible) y `CollectingOutputProvider` (acumula líneas en memoria). `AnalyzerConfig.fromJson` pasa a lanzar `IllegalArgumentException` ante un JSON inválido.

El módulo `cli` depende únicamente de `runner`, lo que impide volver a acoplarlo a lexer/parser.

## Consecuencias

- Una API futura es otro adaptador sobre `runner`: por ejemplo, un endpoint de ejecución usa `ExecutionService` con un `CollectingOutputProvider` y un `ProgrammaticInputProvider` armados desde el request.
- Agregar una operación a la CLI es agregar un `Command` y registrarlo; el menú interactivo lo muestra automáticamente.
- Stdin se lee desde un único `Console`, por lo que el menú interactivo y `readInput` ya no compiten por el buffer de `System.in`.
- Un config de linter inválido ahora falla (exit code 1) en lugar de ignorarse silenciosamente.
- Una operación desconocida ahora termina con exit code 1.
