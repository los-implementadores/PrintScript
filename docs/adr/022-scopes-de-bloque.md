# ADR-022: Los bloques de `if`/`else` tienen scope propio

- **Estado:** Aceptado
- **Fecha:** 2026-10-19
- **Relacionado:** ADR-021 (el análisis semántico corre antes de ejecutar cada sentencia)

## Contexto

`Environment` era un único mapa por programa. El análisis semántico recorría las dos ramas de un `if` sobre esa misma tabla y el intérprete ejecutaba sólo una, así que el chequeo estático no coincidía con lo que pasaba al ejecutar:

- `if (false) { let x: number = 1; } println(x);` pasaba el chequeo semántico y fallaba en runtime con `Variable 'x' is not declared.`, sin posición ni prefijo de error semántico.
- Declarar la misma variable en las dos ramas daba `Variable 'y' is already declared.`, aunque en ejecución sólo corre una.

PrintScript es un subset de TypeScript, y en TypeScript `let` y `const` tienen scope de bloque.

## Decisión

- `Environment` tiene un scope padre. `childScope()` crea el scope de un bloque: ve las variables de afuera y puede reasignarlas, puede declarar las suyas (incluso tapando una de afuera), y lo que declara deja de existir al salir del bloque.
- `SemanticAnalyzerVisitor` e `InterpreterVisitor` analizan y ejecutan cada rama del `if` en un scope hijo, y vuelven al scope anterior al terminar (aunque haya una excepción).
- "Ya declarada" se chequea sólo en el scope actual (`isDeclaredInThisScope`). Leer y asignar buscan hacia afuera.
- Se hizo con TDD: el primer commit agrega los tests en rojo (`BlockScopeTest`, `BlockScopeExecutionTest`) y el siguiente los pone en verde.

## Consecuencias

- El chequeo semántico y la ejecución coinciden: usar una variable fuera del bloque que la declaró es un error semántico, con posición, antes de ejecutar esa sentencia.
- Las dos ramas pueden declarar el mismo nombre.
- El linter (`analyzer`) sigue usando una tabla plana para la regla de variables sin usar: sólo produce advertencias, y una variable que se sombrea en un bloque puede contarse como usada cuando no lo está.
