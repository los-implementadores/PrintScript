# ADR-003: Los nodos del AST son inmutables (`final`)

- **Estado:** Aceptado
- **Fecha:** 2026-08-10

## Contexto

Una vez que el parser construye el AST, ningún módulo debería modificarlo.
El árbol representa la estructura del programa fuente — mutarlo introduciría
bugs difíciles de rastrear cuando varios módulos lo recorren.

## Decisión

Todas las clases concretas de nodos (`NumberLiteral`, `BinaryExpression`,
`VarDeclarationStatement`, etc.) son `final` y no exponen setters.
Los campos se asignan en el constructor y solo se leen mediante getters.
Las colecciones (como la lista de argumentos de `CallExpression` o la lista
de statements de `Program`) se envuelven con `Collections.unmodifiableList`.

## Consecuencias

- El AST es thread-safe por construcción.
- El interpreter y los demás módulos trabajan sobre el mismo árbol sin riesgo de interferencia.
- Para transformaciones del árbol (p.ej. optimizaciones futuras) hay que construir
  nodos nuevos en vez de mutar los existentes.
