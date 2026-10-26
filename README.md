# PrintScript

TP de Ingeniería de Sistemas 2026 — lexer, parser, interpreter, formatter y
static code analyzer para el lenguaje PrintScript (subset de TypeScript).

## Módulos

| Módulo | Responsabilidad |
|--------|-----------------|
| `common` | Tipos compartidos: `Token`, `TokenType`, `Position`, nodos del AST, `Environment`. Sin dependencias externas |
| `lexer` | Análisis léxico. Convierte código fuente en un stream de tokens (Strategy pattern). `LexerFactory` tiene el vocabulario del lenguaje |
| `parser` | Análisis sintáctico. Convierte tokens en AST usando Pratt parsing |
| `interpreter` | Recorre el AST y ejecuta el programa (incluye análisis semántico) |
| `formatter` | Reformatea código fuente según reglas configurables (JSON) |
| `analyzer` | Static code analyzer (linter): reglas configurables (JSON) que reportan violaciones con su posición |
| `runner` | Orquestación del lenguaje: un servicio por operación (validar, analizar, ejecutar, formatear). Puro: sin consola ni archivos |
| `cli` | Adaptador de terminal: comandos (`cli.command`), I/O (`cli.io`) y parseo de argumentos/menú. Depende sólo de `runner` |

Cada módulo depende solo de lo que necesita (ver `build.gradle.kts` de cada uno).

## Cómo correr

Hace falta Git y una JDK 17 o más nueva para ejecutar Gradle. Si no está la JDK 17 que usa el
proyecto para compilar, Gradle la descarga sola.

```bash
./gradlew build          # Compila, verifica formato, Checkstyle y PMD, y corre los tests
./gradlew test           # Solo tests
./gradlew spotlessApply  # Auto-formatear código (google-java-format)
./gradlew spotlessCheck  # Verificar formato
./gradlew checkstyleMain # Verificar estilo
./gradlew pmdMain        # Análisis estático
```

Los git hooks (`hooks/pre-commit` y `hooks/pre-push`) se configuran solos la primera vez que se
compila: Gradle hace `git config core.hooksPath hooks`. No hay que instalarlos a mano.

### CLI

```bash
./gradlew :cli:run --args="execution script.ps"
./gradlew :cli:run --args="execution script_input.ps --version 1.1"
./gradlew :cli:run --args="validation script.ps"
./gradlew :cli:run --args="analyzing script.ps --config linter_rules.json"
./gradlew :cli:run --args="formatting script.ps --config formatter_rules.json"  # sobrescribe el archivo
./gradlew :cli:run       # sin argumentos: menú interactivo
```

## Documentación

- [`docs/ast.md`](docs/ast.md) — Estructura del AST y patrón Visitor
- [`docs/parser.md`](docs/parser.md) — Cómo funciona el parser (Pratt parsing)
- [`docs/formatter.md`](docs/formatter.md) — Formatter y reglas configurables
- [`docs/analyzer.md`](docs/analyzer.md) — Linter, sus reglas y cómo agregar una
- [`docs/tck.md`](docs/tck.md) — Integración con el TCK y publicación de paquetes
- [`docs/adr/`](docs/adr/) — Architecture Decision Records
- [`CHANGELOG.md`](CHANGELOG.md) — Cambios de cada versión

## Tooling

- **Spotless** + google-java-format — formateo automático.
- **Checkstyle** — estilo de código (Google Java Style). Un warning hace fallar el build.
- **PMD** — análisis estático de malas prácticas y complejidad.
- **Git hooks** — pre-commit (formato + Checkstyle + PMD), pre-push (tests). Se saltean con
  `--no-verify`; la red final es el CI.
- **Lockfiles** — cada módulo tiene un `gradle.lockfile` con la versión exacta de todas sus
  dependencias. Si se agrega o actualiza una dependencia, se regeneran con
  `./gradlew resolveAndLockAll --write-locks` y se commitean.
- **CI/CD** — un solo workflow (`.github/workflows/ci.yml`). En un PR corre `./gradlew build`. En
  `main` y en tags además arma el zip de la CLI y publica los módulos en GitHub Packages, a partir
  del mismo build que corrió los tests.

## Versiones y releases

El proyecto usa [Versionado Semántico](https://semver.org/lang/es/) para los paquetes
`org.printscript:*`: MAJOR si se rompe algo que otro usa (por ejemplo, mover una clase pública),
MINOR si se agrega algo compatible y PATCH si se arregla algo. La versión del lenguaje
(PrintScript 1.0 o 1.1) es otra cosa y se elige con `--version`.

`gradle.properties` tiene la próxima versión con `-SNAPSHOT`: es la que se publica en cada push a
`main` y se puede volver a publicar. Una versión sin `-SNAPSHOT` es un release y no cambia más.

Para publicar un release:

1. En [`CHANGELOG.md`](CHANGELOG.md), renombrar `[Unreleased]` a `[X.Y.Z] - fecha` y mergearlo a
   `main` con un PR.
2. Crear el tag y subirlo:
   ```bash
   git tag vX.Y.Z
   git push origin vX.Y.Z
   ```
   El pipeline valida que el tag tenga la forma `vX.Y.Z`, compila con la versión `X.Y.Z`, crea el
   GitHub Release con el zip de la CLI y las notas de esa sección del CHANGELOG, y al final publica
   los paquetes. Si la sección no existe, no publica nada.
3. Subir `gradle.properties` a la siguiente versión `-SNAPSHOT`.

Publicar los paquetes es el último paso porque es el único que no se puede repetir: GitHub
Packages no acepta volver a subir una versión de release. Si ese paso falla a la mitad, antes de
hacer "Re-run" hay que borrar en GitHub Packages las versiones `X.Y.Z` que llegaron a subirse.

## Estado

- [x] Estructura del proyecto
- [x] Lexer (Strategy pattern con TokenMatcher)
- [x] Parser + AST (Pratt parsing, LazyProgram)
- [x] Interpreter + Análisis semántico
- [x] Formatter (reglas configurables desde JSON, estrategia AST y streaming)
- [x] Static analyzer (reglas configurables desde JSON)
- [x] CLI (validation, analyzing, execution, formatting y menú interactivo)
- [x] PrintScript 1.1 (`const`, `boolean`, `if`/`else`, `readInput`, `readEnv`)
- [x] Integración con el TCK (paquetes en GitHub Packages)
- [x] Tooling (Spotless, Checkstyle, PMD, git hooks, lockfiles, CI/CD)
