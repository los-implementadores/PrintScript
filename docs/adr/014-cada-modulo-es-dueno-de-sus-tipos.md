# ADR-014: Cada módulo es dueño de sus tipos y de sus decisiones

- **Estado:** Aceptado
- **Fecha:** 2026-10-19
- **Relacionado:** ADR-001 (AST en `common`), ADR-012 (claves de configuración del formatter)

## Contexto

ADR-001 puso el AST en `common` para que ninguna herramienta dependa del parser. Con el tiempo,
`common` empezó a guardar cosas que no comparte nadie, y algunas decisiones quedaron fuera del
módulo al que pertenecen:

1. **Tipos del linter en `common`.** `AnalyzerConfig`, `NamingConvention`, `Violation` y `Severity`
   solo los usa el linter, pero vivían en `common`. Por eso `common` dependía de Jackson y todos
   los módulos lo arrastraban sin usarlo (viola el Common Reuse Principle). Además, `common` es el
   módulo del que dependen todos, así que debería ser el más estable, y estos tipos cambian cada
   vez que cambian las reglas del linter.
2. **Vocabulario del lenguaje fuera del lexer.** Qué palabras son reservadas y qué caracteres son
   símbolos es una decisión del lexer, pero los mapas se armaban afuera: en `Main`, en seis clases
   de test y en el `AdapterUtils` del TCK. Agregar una keyword obligaba a tocar todas las copias
   (es la clase de decisión que, según Parnas, un módulo tiene que esconder).
3. **Dependencias de más.** `analyzer` declaraba `lexer` y `parser` sin usarlos, y había dos
   librerías de JSON para lo mismo: Jackson en el linter y Gson en el formatter.

## Decisión

- `AnalyzerConfig` y `NamingConvention` pasan a `org.printscript.analyzer.config`; `Violation` y
  `Severity`, a `org.printscript.analyzer.violation`. `common` queda sin dependencias externas: solo
  tokens, posiciones, AST, `Environment` y `LanguageVersion`.
- El vocabulario de PrintScript vive en `org.printscript.lexer.LexerFactory`. `create(reader)` arma
  el lexer completo y `defaultMatchers()` devuelve los matchers para quien quiera sumar otros.
- `analyzer` depende solo de `common` (y de Jackson).
- El formatter lee su configuración con Jackson (`@JsonProperty` + `@JsonAlias`). Un test
  (`FormattingRulesTest`) fija cómo se leen las claves propias y las del TCK; corrido contra la
  versión con Gson da el mismo resultado, salvo el tipo de excepción ante un JSON mal formado.

Se descartó dejar clases "puente" deprecadas en `common`: `common` no puede depender de `analyzer`
(sería un ciclo), así que el puente tendría que duplicar el código y mantener Jackson en `common`,
que es justo lo que se quiere sacar.

## Consecuencias

- **Cambio incompatible para quien usa los paquetes publicados.** Los imports de
  `org.printscript.common.configs` y `org.printscript.common.linterViolations` dejan de existir. Por
  SemVer la versión pasa de `1.1.0-SNAPSHOT` a `2.0.0-SNAPSHOT`. El TCK depende de `1.1.0-SNAPSHOT`,
  así que no se rompe cuando se publique este cambio; para pasar a 2.x tiene que actualizar esos
  imports y puede reemplazar su `buildMatchers()` por `LexerFactory.defaultMatchers()`.
- Agregar una keyword o un símbolo se hace en un solo lugar.
- Los tests de parser y formatter usan el vocabulario real del lenguaje en vez de un subconjunto.
- Una sola librería de JSON en el proyecto.
