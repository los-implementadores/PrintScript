# PrintScript

TP de Ingeniería de Sistemas (Austral, 2026) — lexer, parser, interpreter, formatter y
static code analyzer para el lenguaje PrintScript (subset de TypeScript).

## Módulos

- `common`: tipos compartidos entre módulos (`Token`, `TokenType`, `Position`, nodos de AST más adelante).
- `lexer`: análisis léxico. Convierte el código fuente en un stream de tokens.
- `parser`: análisis sintáctico. Convierte el stream de tokens en un AST.
- `interpreter`: recorre el AST y ejecuta el programa.
- `formatter`: reformatea código fuente según reglas configurables.
- `analyzer`: static code analyzer, detecta incumplimiento de reglas configurables.
- `cli`: punto de entrada. Orquesta los módulos según la operación pedida
  (Validation, Execution, Formatting, Analyzing).

Cada módulo depende solo de lo que necesita (ver `build.gradle.kts` de cada uno).

## Cómo correr

Abrir la carpeta en IntelliJ IDEA (File > Open) — importa el proyecto Gradle y descarga
todo lo necesario automáticamente.

Desde terminal (necesita Gradle instalado, o generar el wrapper una vez con
`gradle wrapper` desde IntelliJ o localmente):

```
./gradlew build
./gradlew test
```

## Estado

- [x] Estructura del proyecto
- [ ] Lexer
- [ ] Parser + AST
- [ ] Interpreter
- [ ] CLI (modo Execution)
- [ ] Formatter
- [ ] Static analyzer
