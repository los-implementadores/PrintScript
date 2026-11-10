# ADR-025: Las versiones del lenguaje solo las conoce el runner

- **Estado:** Aceptado
- **Fecha:** 2026-11-10
- **Relacionado:** ADR-008 (parselets), ADR-019 (registro de funciones nativas), ADR-021 (servicios del runner), ADR-024 (separar `common`)

## Contexto

Para soportar PrintScript 1.1, cada módulo aprendió que el lenguaje tiene versiones: el parser
armaba una gramática distinta con `ParserGrammar.forVersion(version)` (con `if` por versión
adentro), y cada función nativa declaraba `since()` para que `BuiltinFunctions.resolve` la
rechazara en 1.0. `LanguageVersion` vivía en `common` y lo recibían el parser, el semántico y el
intérprete.

Así, agregar una versión obligaba a tocar varios módulos, y la definición de "qué es la 1.1"
quedaba repartida: los tipos y las sentencias en el parser, las funciones en cada clase de
función. Además, cada módulo quedaba atado a una noción que no es suya: al parser le alcanza con
saber qué construcciones tiene que entender, no por qué.

## Decisión

Los módulos no conocen las versiones. Cada uno ofrece piezas y el runner las combina:

- **Parser:** `ParserGrammar` se arma con `GrammarFeature` (`CORE`, `BOOLEANS`, `CONSTANTS`,
  `CONDITIONALS`), el único lugar donde se registra qué parselet maneja cada construcción. Los
  tipos admitidos también son parte de la gramática (`ParseContext.parseTypeName()`). Se sacan
  `forVersion` y los constructores de `ParserImpl` que elegían una versión.
- **Intérprete:** `BuiltinFunction` ya no tiene `since()`. El semántico y el intérprete reciben el
  `BuiltinFunctions` con las funciones que existen.
- **Runner:** `LanguageVersion` (`org.printscript.runner`) es el único lugar que dice qué trae cada
  versión. Cada constante tiene sus features de gramática y sus funciones nativas, y los servicios
  le piden `version.grammar()` y `version.builtins()` al armar cada pieza.

```java
V1_0("1.0", List.of(CORE), List.of(new PrintlnFunction())),
V1_1("1.1", List.of(CORE, BOOLEANS, CONSTANTS, CONDITIONALS),
     List.of(new PrintlnFunction(), new ReadInputFunction(), new ReadEnvFunction()));
```

## Consecuencias

- Agregar una versión es agregar una constante en `LanguageVersion`. Agregar una construcción es
  un parselet en una `GrammarFeature` (o una feature nueva) y sumarla a las versiones que la tengan.
- Los tests de cada módulo prueban features, no versiones; qué acepta cada versión se prueba en el
  runner (`LanguageVersionTest`).
- En 1.0, `readInput(...)` falla con "Unknown function 'readInput'" en vez de "only supported in
  PrintScript 1.1": en 1.0 la función no existe. El error sigue saliendo con su posición.
- **Cambio incompatible:** `ParserImpl(tokens)`, `ParserImpl(tokens, version)`,
  `ParserGrammar.forVersion`, `BuiltinFunction.since()` y los constructores del intérprete y el
  semántico que recibían la versión dejan de existir. Quien use los módulos sueltos (el TCK) tiene
  que armar la gramática y las funciones, o mejor, usar el runner.
