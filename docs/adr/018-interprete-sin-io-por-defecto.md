# ADR-018: El intérprete recibe su I/O en un solo constructor, sin defaults a `System.*`

- **Estado:** Aceptado
- **Fecha:** 2026-10-19
- **Relacionado:** ADR-016 (Separar la CLI en capas)

## Contexto

ADR-016 agregó `OutputProvider` para que `println` no escribiera directo a `System.out`, pero `InterpreterImpl` e `InterpreterVisitor` seguían teniendo cinco constructores encadenados. Los cortos completaban lo que faltaba con `StdinInputProvider`, `SystemEnvProvider` y `StdoutOutputProvider`, así que la dependencia con `System.*` seguía viviendo adentro del intérprete. Además la versión por defecto era inconsistente: `InterpreterVisitor(env)` usaba 1.0 y `InterpreterVisitor(env, input)` usaba 1.1.

En la CLI pasaba algo parecido: `ExecutionCommand` hacía `new SystemEnvProvider()` adentro, mientras que `Console` y `FileAccess` se le inyectaban.

## Decisión

- `ExecutionIo` (input, variables de entorno, output) pasa del módulo `runner` al módulo `interpreter`, que es quien lo consume.
- `InterpreterVisitor` tiene un solo constructor: `(Environment, ExecutionIo, LanguageVersion)`. `InterpreterImpl` también, salvo por un constructor `@Deprecated` `(Environment, InputProvider, EnvProvider, LanguageVersion)` que se mantiene porque lo usa el adapter del TCK (que captura la salida reemplazando `System.out`). Ese es el único lugar del intérprete que todavía elige `StdoutOutputProvider`, y está marcado.
- `ExecutionCommand` recibe el `EnvProvider` por constructor. `Main` es el único lugar de la CLI que elige implementaciones atadas al sistema (`SystemConsole`, `LocalFileAccess`, `SystemEnvProvider`).
- Los tests del intérprete inyectan un `CollectingOutputProvider` en lugar de redirigir `System.out`.

## Consecuencias

- Construir un intérprete obliga a decidir explícitamente de dónde lee y a dónde escribe; no hay forma de terminar escribiendo en la consola sin querer.
- Los tests del intérprete ya no tocan estado global, así que pueden correr en paralelo.
- Cuando el adapter del TCK pase a inyectar un `OutputProvider`, se puede borrar el constructor deprecado.
