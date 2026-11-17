package org.printscript.formatter;

/**
 * Dónde está una brecha dentro del programa. Es el {@code Shape} de rustfmt reducido a lo que usan
 * las reglas de PrintScript; si se agrega un ancho máximo de línea, el ancho disponible va acá.
 *
 * @param depth cuántos bloques contienen la brecha (la llave que cierra un bloque ya está afuera)
 * @param lineIndent la indentación con la que quedó escrita la línea actual
 */
public record Shape(int depth, String lineIndent) {}
