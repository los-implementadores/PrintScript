# ADR-018: Servicios del runner con dependencias inyectadas y un único recorrido por sentencias

- **Estado:** Aceptado
- **Fecha:** 2026-10-19
- **Relacionado:** ADR-013 (Separar la CLI en capas), ADR-015 (Intérprete sin I/O por defecto), ADR-017 (`Program` de una sola pasada)

## Contexto

ADR-013 creó un servicio por operación en el módulo `runner`, pero cada servicio construía sus piezas con `new` (`new SemanticAnalyzerImpl(...)`, `new InterpreterImpl(...)`, `new StaticAnalyzerImpl(...)`, `new FormatterImpl(...)`) y cargaba el programa con métodos estáticos (`ProgramLoader.load`). Las interfaces `SemanticAnalyzer`, `Interpreter`, `StaticAnalyzer` y `Formatter` existían, pero nadie las usaba para inyectar: no se podía probar un servicio sin el lexer, el parser y el intérprete reales.

Además, el bucle "por cada sentencia: análisis semántico y después X" estaba copiado en `ExecutionService` y en `AnalysisService`, y `ValidationService` recorría el programa por su cuenta. Cualquier cambio en cómo se recorre el programa (por ejemplo, mostrar el progreso del parsing, que pide la consigna) había que repetirlo en cada servicio.

## Decisión

- Cada servicio recibe por constructor lo que necesita, como interfaces:
  - `ProgramLoader`: código fuente → `Program`. La implementación es `LazyProgramLoader` (lexer + parser + `LazyProgram`).
  - Fábricas `SemanticAnalyzerFactory`, `InterpreterFactory`, `LinterFactory` y `FormatterFactory`. Son fábricas y no instancias porque el analizador semántico, el intérprete y el linter guardan estado de un programa (tabla de símbolos, memoria, violaciones): cada operación necesita uno nuevo.
- `StatementPipeline` es el único lugar que recorre el programa. Cada servicio sólo declara sus etapas: `ValidationService` ninguna (sólo parsea), `AnalysisService` semántico + linter, `ExecutionService` semántico + intérprete.
- `LanguageServices.printScript()` es el único lugar del módulo que elige implementaciones concretas. `Main` se lo pasa a `CliApplication`.
- Las fábricas, el loader y el pipeline son package-private, igual que los constructores de los servicios. Hacia afuera, `runner` expone los servicios y `LanguageServices`; así la CLI no ve tipos del lexer, del parser ni del linter (como pedía ADR-013).

## Consecuencias

- Los servicios se pueden probar con dobles. `ExecutionServiceTest` verifica que cada sentencia se analiza justo antes de ejecutarse, y que una sentencia con error semántico no se ejecuta, sin lexer, parser ni intérprete reales.
- Un cambio en cómo se recorre el programa se hace una sola vez, en `StatementPipeline`. Por ejemplo, que la validación también chequee semántica es agregarle una etapa a `ValidationService`.
- Quien quiera los servicios con otras implementaciones tiene que hacerlo desde el paquete `runner`; hoy no hace falta, y abrirlo después es sólo cambiar visibilidades.
