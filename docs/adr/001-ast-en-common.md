# ADR-001: Los nodos del AST viven en el módulo `common`

- **Estado:** Aceptado
- **Fecha:** 2026-08-10

## Contexto

El AST es producido por el `parser` pero consumido por múltiples módulos:
`interpreter`, `formatter` y `analyzer`. Si los nodos vivieran en `parser`,
todos esos módulos deberían depender de `parser` para poder trabajar con el árbol,
generando acoplamiento innecesario.

## Decisión

Los nodos del AST (`Node`, `Expression`, `Statement`, `Program` y todas sus
implementaciones) se ubican en el módulo `common`, bajo el paquete
`org.printscript.common.ast`.

## Consecuencias

- El `interpreter`, `formatter` y `analyzer` dependen solo de `common`, no de `parser`.
- El `parser` también depende de `common` para construir los nodos — no hay ciclos.
- Cualquier módulo nuevo que necesite recorrer el árbol solo agrega `common` como dependencia.
