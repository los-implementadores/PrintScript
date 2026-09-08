# ADR-010: Reutilizar CallExpression para readInput

- **Estado:** Aceptado
- **Fecha:** 2026-09-08
- **Relacionado:** ADR-002 (Visitor para recorrer el AST), ADR-008 (Despacho del parser por parselets), ADR-009 (Limitación OCP del Visitor)

## Contexto

En PrintScript 1.1 se introduce la función built-in `readInput(msg)` para solicitar entradas al usuario.
Sintácticamente se invoca como una llamada a función estándar (`readInput("Ingrese un valor: ")`).
El issue #34 plantea explícitamente: *"reconocer readInput como expresión tipo llamada (reutilizar CallExpression o nodo propio ReadInputExpression; decidir y documentar)"*.

## Opciones consideradas

1. **Crear un nodo dedicado `ReadInputExpression` en el AST**:
   - Modela explícitamente en el AST la lectura de entrada.
   - **Desventaja principal**: Según lo analizado en el **ADR-009** (*Limitación OCP del Visitor*), incorporar un nuevo tipo de nodo al AST obliga a modificar la interfaz `ASTVisitor<T>` y todas sus implementaciones en todos los módulos (`InterpreterVisitor`, `SemanticAnalyzerVisitor`, `FormatterVisitor`, `AnalyzerVisitor`), además de requerir parselets o tokens dedicados en el parser.

2. **Reutilizar `CallExpression` con callee `"readInput"`**:
   - `CallExpression` ya existe en `common.ast` y modela cualquier llamada con nombre y argumentos (`callee`, `arguments`).
   - El parser (`IdentifierParselet`) ya lo parsea de forma natural sin requerir nuevos tokens ni gramática adicional.
   - El módulo `formatter` ya sabe formatear llamadas `callee(args)` sin tocar una sola línea.
   - El módulo `analyzer` (Linter) puede validar la regla de argumentos simples sobre `readInput` de la misma manera que ya lo hace con `println`.
   - Es completamente consistente con `println`, que también se modela como un `CallExpression`.
   - Sienta la base para futuras funciones built-in de 1.1 con sintaxis de llamada, como `readEnv(name)`.

## Decisión

Se decide **reutilizar `CallExpression`** para `readInput`.

Las validaciones específicas y el comportamiento en tiempo de ejecución se resuelven en:
- **`SemanticAnalyzerVisitor`**:
  - Verifica que solo se invoque en `LanguageVersion.V1_1` (en 1.0 se rechaza con error semántico indicando posición).
  - Valida que reciba exactamente 1 argumento y que sea de tipo `string`.
  - Resuelve contextualmente el tipo resultante compatible con el destino (`string`, `number` o `boolean`, o `string` si se usa dentro de `println`).
- **`InterpreterVisitor`**:
  - Imprime el mensaje (prompt) por la salida configurada antes de leer.
  - Solicita el valor a través de la abstracción inyectable `InputProvider`.
  - Realiza la coerción al tipo esperado (`string`, `number` -> `Double`, `boolean` -> `Boolean`), fallando con error de ejecución y posición si el valor no se puede interpretar en el tipo esperado.

## Consecuencias

- **OCP preservado**: No se altera `ASTVisitor<T>`, evitando refactorizaciones en módulos como `formatter`.
- **Cero cambios sintácticos en el parser**: `readInput(...)` es reconocido por la infraestructura existente.
- **Consistencia en el ecosistema**: Todas las funciones built-in del lenguaje se tratan de forma homogénea.
- **Detección temprana de incompatibilidad de versión**: La versión se verifica de forma semántica con mensaje de error y posición precisos.
