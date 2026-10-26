# ADR-023: El formatting de la CLI escribe en streaming

- **Estado:** Aceptado
- **Fecha:** 2026-10-19
- **Relacionado:** ADR-012 (Formateador en streaming), ADR-020 (`Program` de una sola pasada)

## Contexto

La consigna pide que el sistema soporte fuentes que no entran en memoria. La operación `Formatting` de la CLI usaba `Formatter.format(Program)`, que arma el código formateado entero en un `String` (antes, además, materializaba todas las sentencias con `toList()`), y después `FileAccess.write` escribía ese `String` de una vez. Con un script de 16 MB (600 000 sentencias) la CLI terminaba con `OutOfMemoryError` usando 16 MB de heap.

ADR-012 agregó un formateador streaming basado en texto, pero se comporta distinto del formateador de AST que usa la CLI: con la configuración por defecto no cambia los espacios, y el salto de línea de `println` lo pone en otro lugar. Pasar la CLI a ese formateador cambiaba la salida del comando.

## Decisión

- El formateador de AST escribe de a una sentencia: `Formatter.format(Program, Writer)` y `FormatterVisitor.writeProgram`. `format(Program)` sigue existiendo y usa lo mismo sobre un `StringWriter`.
- `FormattingService.format(Reader, LanguageVersion, FormattingRules, Writer)` parsea, formatea y escribe cada sentencia antes de pedir la siguiente.
- `FileAccess.rewrite(path, contenido)` reemplaza a `write(path, String)`. `LocalFileAccess` escribe en un archivo temporal al lado del original y lo mueve encima sólo si el formateo terminó bien. Si hay un error de sintaxis a mitad del archivo, el original queda intacto y no queda el temporal.

## Consecuencias

- El mismo script de 16 MB se formatea con 8 MB de heap. La salida del comando no cambia: los tests E2E de formatting pasan sin tocar lo que esperan.
- Leer el original mientras se escribe el nuevo funciona porque se escribe en otro archivo.
- Siguen existiendo dos formateadores con reglas distintas: la CLI usa el de AST y el TCK usa el streaming de texto. Unificarlos es una decisión aparte.
