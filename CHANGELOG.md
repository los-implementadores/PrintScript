# Changelog

Todos los cambios importantes del proyecto se anotan en este archivo.

El formato sigue [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/) y el proyecto usa
[Versionado Semántico](https://semver.org/lang/es/). Las versiones son las de los paquetes
`org.printscript:*`, no las del lenguaje: todas las versiones desde la 1.1.0 implementan
PrintScript 1.0 y 1.1.

Las notas de cada GitHub Release salen de la sección de su versión en este archivo; un tag sin
sección no se publica (ver "Versiones y releases" en el README).

> La 1.0.0 se cerró antes de que el proyecto usara tags: su fecha es la del merge que la completó
> en `main` (`6916f79`). La 1.1.0 se completó el 27/09 (`50d833c`) y se publicó el 19/10 con el tag
> `v1.1.0`, el primero del proyecto.

## [Unreleased]

### Cambiado

- **Incompatible:** `common` se separa en tres módulos (ADR-024): `source` (`Position`,
  `PrintScriptException`), `token` (`Token`, `TokenType`) y `ast` (los nodos, `Program` y
  `ASTVisitor`). `Environment` pasa a `org.printscript.interpreter.scope` y `LanguageVersion` a
  `org.printscript.runner`. El artefacto `org.printscript:common` deja de publicarse.
- **Incompatible:** los módulos no saben de versiones del lenguaje (ADR-025). El parser se arma con
  una `ParserGrammar` hecha de `GrammarFeature` (`CORE`, `BOOLEANS`, `CONSTANTS`, `CONDITIONALS`) y
  el intérprete con el `BuiltinFunctions` que se le pase; `LanguageVersion`, en el runner, dice qué
  trae cada versión. Se van `ParserGrammar.forVersion`, `BuiltinFunction.since()` y los
  constructores que recibían la versión. En 1.0, `readInput` falla con "Unknown function".
- **Incompatible:** las reglas del formatter y del linter se registran con su clave en un solo
  lugar (`FormattingRules` y `LintRules`) y se activan desde un `RuleConfig` (módulo `rules`), que
  reemplaza a `AnalyzerConfig` y a la clase de configuración del formatter (ADR-026). Agregar una
  regla es su clase más una línea en el registro.
- **Incompatible:** el formatter trabaja sobre los tokens del código, espacios incluidos, en vez
  del AST (ADR-027). Hay un solo formateador para la CLI y el TCK: sin reglas devuelve el código
  igual, y una regla que no está en la configuración no lo toca. Sin configuración, la CLI usa un
  estilo por defecto con las reglas de la consigna. La salida ya no agrega un salto de línea al
  final y respeta los números como están escritos. `newlineBeforePrintln` (líneas antes del
  `println`) y `line-breaks-after-println` (después) pasan a ser reglas distintas.
- **Incompatible:** el linter recorre el árbol con `Node.children()` en vez de un visitor (ADR-027).
  `LintRule` tiene `onEnter`, `onExit` y `onFinish`; un tipo de nodo nuevo ya no obliga a tocarlo.
- El AST tiene un nodo `Block` para los bloques `{ }`, y `Program` deja de ser un nodo.
- **Incompatible:** `Violation` y `Severity` pasan de `org.printscript.common.linterViolations` a
  `org.printscript.analyzer.violation`, `NamingConvention` pasa a `org.printscript.analyzer.rule` y
  `AnalyzerConfig` (antes en `org.printscript.common.configs`) se reemplaza por `RuleConfig`. Por eso
  la próxima versión es la 2.0.0 (ADR-014, ADR-026).
- **Incompatible:** el lexema de un `STRING_LITERAL` incluye las comillas, tal como está en el
  código; el parser saca el valor de adentro.
- El linter funciona con reglas (`LintRule`) registradas en `LintRules`, y `StaticAnalyzerImpl`
  acepta una fábrica de reglas propias (ADR-015).
- La configuración se lee con Jackson en lugar de Gson. Solo acepta JSON estándar (Gson también
  aceptaba comentarios, comillas simples y claves sin comillas), y un JSON mal formado lanza
  `IllegalArgumentException`.
- `analyzer` ya no depende de `lexer` ni de `parser`.
- Checkstyle usa la indentación de google-java-format y un warning hace fallar el build.
- CI y CD son un solo workflow: lo que se publica sale del mismo build que corrió los tests.
- Los git hooks se configuran solos (`core.hooksPath`) la primera vez que se compila, salvo que
  ya haya otro `core.hooksPath` configurado.
- El ADR del Strategy del lexer, que estaba duplicado como ADR-004, pasa a ser el ADR-013.

### Agregado

- 5 reglas de estilo propias para el código Java, compartidas por Spotless (que las arregla) y
  Checkstyle (que las verifica), definidas una sola vez en `buildSrc` (ADR-028).
- La regla `maxOneSpaceBetweenTokens` del formatter (como mucho un espacio entre tokens).
- `LexerFactory.createWithWhitespace`, que también devuelve los espacios como tokens.
- `LexerFactory` en el módulo `lexer`, con el vocabulario de PrintScript en un solo lugar.
- Lockfiles de dependencias (`gradle.lockfile`), checksum del Gradle wrapper y descarga automática
  de la JDK 17 si no está instalada.
- Este CHANGELOG.

### Arreglado

- La posición de un `if` terminaba en el token siguiente al bloque en vez de en su `}`.
- Un valor del tipo equivocado en la configuración del formatter o del linter
  (`"indentSize": "cuatro"`) ahora es un error con el nombre de la clave.
- `getViolations()` repetía los avisos de variables sin usar si se llamaba más de una vez.
- La regla de nombres rechazaba nombres válidos con dígitos o siglas (`value1`, `var1`, `myURL`).
- `"namingConventionFormat": null` en la configuración del linter terminaba en un
  `NullPointerException`.
- `./gradlew :cli:run` sin argumentos no podía leer el menú interactivo: Gradle no le pasaba stdin.
- Documentación desactualizada: README, `docs/analyzer.md`, `docs/ast.md`, `docs/tck.md` y ADR-005.

### Eliminado

- La dependencia a Gson.

## [1.1.0] - 2026-10-19

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
