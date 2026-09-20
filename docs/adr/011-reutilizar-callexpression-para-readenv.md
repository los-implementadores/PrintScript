# ADR-011: Reutilizar CallExpression para readEnv

- **Estado:** Aceptado
- **Fecha:** 2026-09-08
- **Relacionado:** ADR-002 (Visitor para recorrer el AST), ADR-008 (Despacho del parser por parselets), ADR-009 (Limitación OCP del Visitor), ADR-010 (Reutilizar CallExpression para readInput)

## Contexto

En PrintScript 1.1 se introduce la función built-in `readEnv(name)` para consultar variables del entorno de ejecución.
Sintácticamente se invoca como una llamada a función estándar (`readEnv("API_KEY")`).
El issue #35 plantea: *"reconocer readEnv como expresión tipo llamada (reutilizar CallExpression o nodo propio ReadEnvExpression; decidir y documentar, consistente con readInput #34)"*.

## Opciones consideradas

1. **Crear un nodo dedicado `ReadEnvExpression` en el AST**:
   - Como se documentó en los ADR-009 y ADR-010, agregar un nodo nuevo al AST obligaría a modificar `ASTVisitor<T>` y todos los visitors del sistema (`InterpreterVisitor`, `SemanticAnalyzerVisitor`, `FormatterVisitor`, `AnalyzerVisitor`), violando el principio Open/Closed para los visitors existentes.

2. **Reutilizar `CallExpression` con callee `"readEnv"`**:
   - `CallExpression` ya existe en `common.ast` y modela uniformemente cualquier llamada con nombre y argumentos (`callee`, `arguments`).
   - El parser (`IdentifierParselet`) ya lo parsea sin requerir ningún token adicional ni modificaciones gramaticales.
   - El `formatter` lo formatea automáticamente de forma correcta (`readEnv(...)`).
   - El `analyzer` (Linter) puede aplicar las reglas sobre funciones built-in de forma homogénea.
   - Es 100% consistente con `println` y `readInput` (ADR-010).

## Decisión

Se decide **reutilizar `CallExpression`** para representar `readEnv`.

La lógica se gestiona en:
- **`SemanticAnalyzerVisitor`**:
  - Verifica que solo se invoque en `LanguageVersion.V1_1` (en 1.0 se rechaza con error semántico indicando posición).
  - Valida que reciba exactamente 1 argumento y que sea de tipo `string`.
  - Resuelve el tipo resultante según el contexto de asignación (`string`, `number` o `boolean`) o dentro de `println`.
- **`InterpreterVisitor`**:
  - Consulta el valor de la variable a través de la abstracción inyectable `EnvProvider`.
  - Si la variable no está definida, lanza un error de ejecución indicando nombre de la variable y posición.
  - Realiza la coerción al tipo esperado (`string`, `number`, `boolean`), fallando si el valor no se puede interpretar en el tipo destino.

## Consecuencias

- **OCP preservado**: Ningún módulo externo al runtime (`formatter`, `analyzer`) se ve afectado.
- **Homogeneidad**: Todas las funciones del lenguaje (`println`, `readInput`, `readEnv`) se representan de forma consistente.
- **Testabilidad desacoplada**: `EnvProvider` permite inyectar mapas de variables en tests sin depender de `System.getenv`.
