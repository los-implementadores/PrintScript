# ADR-006: Fail-fast con `ParseException`

- **Estado:** Aceptado
- **Fecha:** 2026-08-11

## Contexto

Cuando el parser encuentra un token inesperado, necesita una estrategia de
manejo de errores. Las opciones consideradas fueron:

- **Fail-fast**: lanzar una excepción en el primer error con la posición exacta.
- **Error recovery**: sincronizar el parser (p.ej. avanzar hasta el próximo `;`)
  e intentar seguir parseando para reportar múltiples errores en una pasada.

## Decisión

Se usa la estrategia fail-fast. El parser lanza `ParseException` en cuanto
detecta el primer error sintáctico. La excepción incluye:

- Un mensaje descriptivo que indica qué se esperaba y qué se encontró.
- La `Position` del token problemático (fila y columna exactas).

```java
throw new ParseException(
    "Expected SEMICOLON but found '" + current.getLexeme() + "'",
    current.getPosition()
);
```

## Consecuencias

- La implementación es simple y predecible: no hay lógica de recuperación que
  pueda generar errores en cascada confusos.
- El mensaje de error siempre apunta al primer problema real en el código fuente.
- Consistente con la estrategia de `LexerException` en el módulo `lexer`.
- La limitación es que el usuario solo ve un error por ejecución. Para un
  lenguaje simple como PrintScript 1.0 esto es aceptable; si en versiones futuras
  se quiere reportar múltiples errores, se puede agregar error recovery sin
  cambiar la interfaz `Parser`.
