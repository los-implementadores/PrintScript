# Changelog

Todos los cambios importantes del proyecto se anotan en este archivo.

El formato sigue [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/) y el proyecto usa
[Versionado Semántico](https://semver.org/lang/es/). Las versiones son las de los paquetes
`org.printscript:*`, no las del lenguaje: todas las versiones desde la 1.1.0 implementan
PrintScript 1.0 y 1.1.

Las notas de cada GitHub Release salen de la sección de su versión en este archivo; un tag sin
sección no se publica (ver "Versiones y releases" en el README).

> Las versiones 1.0.0 y 1.1.0 se cerraron antes de que el proyecto usara tags. Sus fechas son las
> del merge que completó cada una en `main`: `6916f79` para la 1.0.0 y `50d833c` para la 1.1.0.

## [Unreleased]

### Cambiado

- **Incompatible:** `AnalyzerConfig` y `NamingConvention` pasan de `org.printscript.common.configs`
  a `org.printscript.analyzer.config`, y `Violation` y `Severity` pasan de
  `org.printscript.common.linterViolations` a `org.printscript.analyzer.violation`. `common` queda
  sin dependencias externas. Por eso la próxima versión es la 2.0.0 (ADR-014).
- El linter funciona con reglas (`LintRule`) registradas en `LintRules`. Agregar una regla ya no
  modifica `AnalyzerVisitor`, y `StaticAnalyzerImpl` acepta una fábrica de reglas propias (ADR-015).
- El formatter lee su configuración con Jackson en lugar de Gson. Solo acepta JSON estándar (Gson
  también aceptaba comentarios, comillas simples y claves sin comillas), y un JSON mal formado
  lanza `IllegalArgumentException`.
- `analyzer` ya no depende de `lexer` ni de `parser`.
- Checkstyle usa la indentación de google-java-format y un warning hace fallar el build.
- CI y CD son un solo workflow: lo que se publica sale del mismo build que corrió los tests.
- Los git hooks se configuran solos (`core.hooksPath`) la primera vez que se compila.
- El ADR del Strategy del lexer, que estaba duplicado como ADR-004, pasa a ser el ADR-013.

### Agregado

- `LexerFactory` en el módulo `lexer`, con el vocabulario de PrintScript en un solo lugar.
- Lockfiles de dependencias (`gradle.lockfile`), checksum del Gradle wrapper y descarga automática
  de la JDK 17 si no está instalada.
- Este CHANGELOG.

### Arreglado

- `getViolations()` repetía los avisos de variables sin usar si se llamaba más de una vez.
- Documentación desactualizada: README, `docs/analyzer.md`, `docs/ast.md`, `docs/tck.md` y ADR-005.

### Eliminado

- La dependencia a Gson.

## [1.1.0] - 2026-09-27

### Agregado

- PrintScript 1.1: `const`, tipo `boolean`, `if`/`else`, `readInput` y `readEnv` (#46, #47, #48,
  #51, #52).
- Versión del lenguaje elegible (`--version 1.0|1.1`) en el parser, el semántico y el intérprete
  (#45).
- Formatter: reglas para `if` (#49) y estrategia en streaming con reglas aisladas para el TCK (#55).
- Linter: regla de argumento simple en `readInput` (#54).
- Publicación de los módulos en GitHub Packages (#53, #56).

## [1.0.0] - 2026-09-08

### Agregado

- Lexer, parser (Pratt parsing con parselets), intérprete con análisis semántico, formatter, linter
  y CLI para PrintScript 1.0.
- Procesamiento en streaming: el lexer y el parser son iteradores, y el intérprete ejecuta sentencia
  por sentencia.
- Spotless, Checkstyle, PMD, git hooks, CI y CD con GitHub Actions.
