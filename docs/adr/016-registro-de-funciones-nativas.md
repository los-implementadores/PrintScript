# ADR-016: Registro de funciones nativas

- **Estado:** Aceptado
- **Fecha:** 2026-10-19
- **Relacionado:** ADR-009 (Limitación OCP del Visitor), ADR-010 y ADR-011 (Reutilizar `CallExpression` para `readInput` y `readEnv`)

## Contexto

ADR-010 y ADR-011 decidieron representar `readInput` y `readEnv` como `CallExpression` para no agregar nodos al AST. Eso evitó tocar todos los visitors, pero el despacho quedó en `if (callee.equals("println")) ... else if (callee.equals("readInput")) ...`, repetido en `SemanticAnalyzerVisitor` y en `InterpreterVisitor`. Las reglas de cada función (desde qué versión existe, cuántos argumentos recibe) también estaban duplicadas en los dos visitors, con mensajes distintos. Agregar una función obligaba a modificar los dos.

## Decisión

Cada función nativa es una `BuiltinFunction` (paquete `interpreter.builtin`) que concentra todo lo que el lenguaje sabe de ella:

- `name()`, `since()` (primera versión en la que existe) y `arity()`.
- `checkTypes(TypeChecker, CallExpression)`: el chequeo de tipos, que usa el análisis semántico.
- `call(CallContext, CallExpression)`: la ejecución, que usa el intérprete.

`BuiltinFunctions` las indexa por nombre y valida la existencia, la versión y la aridad en un único lugar (`resolve`). Cada visitor decide qué excepción lanzar (`SemanticException` o `ExecutionException`). `TypeChecker` y `CallContext` son las interfaces mínimas que cada visitor le ofrece a una función, para que no dependa del visitor entero.

`readInput` y `readEnv` comparten la clase base `ExternalTextFunction` (leer un texto de afuera y convertirlo al tipo que espera el contexto); cada una sólo define de dónde sale el texto.

Los visitors reciben el registro por constructor. `InterpreterImpl` y `SemanticAnalyzerImpl` usan `BuiltinFunctions.standard()`.

## Consecuencias

- Agregar una función al lenguaje es escribir una clase y registrarla en `BuiltinFunctions.standard()`. Ni el análisis semántico ni el intérprete cambian; hay un test que lo muestra con una función `upper` de ejemplo.
- Las reglas de versión y aridad son las mismas en los dos visitors. El intérprete ahora usa los mismos mensajes que el semántico.
- El linter (`analyzer`) sigue teniendo su propia regla para `println`/`readInput`: es una regla de estilo configurable, no parte de la semántica de la función.
