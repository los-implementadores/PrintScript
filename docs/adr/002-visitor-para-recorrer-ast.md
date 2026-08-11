# ADR-002: Patrón Visitor para recorrer el AST

- **Estado:** Aceptado
- **Fecha:** 2026-08-10

## Contexto

El `interpreter`, el `formatter` y el `analyzer` necesitan recorrer el mismo AST
pero hacer cosas completamente distintas en cada nodo. Las opciones consideradas fueron:

- **instanceof + casting** en cada módulo: no escala, rompe cuando se agrega un nodo nuevo.
- **Métodos en los nodos** (`evaluate()`, `format()`...): mezcla responsabilidades, viola SRP.
- **Patrón Visitor**: cada módulo implementa su propia lógica de recorrido sin tocar los nodos.

## Decisión

Se agregó la interfaz `ASTVisitor<T>` en `common` y un método `accept(ASTVisitor<T>)`
en cada nodo del AST. Cada módulo que necesite recorrer el árbol implementa `ASTVisitor`.

## Consecuencias

- Agregar un módulo nuevo (p.ej. un pretty-printer) no requiere tocar ningún nodo.
- Agregar un nodo nuevo al AST obliga a actualizar todos los visitors existentes —
  el compilador lo detecta en el momento.
- El tipo genérico `<T>` permite que cada visitor retorne el tipo que necesita
  (`Object` para el interpreter, `String` para el formatter, etc.).
