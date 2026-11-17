package org.printscript.formatter;

import org.printscript.ast.Node;

/**
 * El espacio entre dos partes de un nodo del AST: lo único que cambian las reglas de formato.
 *
 * <p>No es «el espacio entre dos tokens» sino un espacio con significado: {@link #kind()} dice cuál
 * es (antes del {@code :} de una declaración, entre dos sentencias) y {@link #node()} de qué nodo
 * es.
 *
 * @param kind qué espacio es
 * @param node el nodo al que pertenece; en las brechas entre sentencias, la sentencia que sigue; en
 *     las del bloque, el bloque; {@code null} al final del archivo y en un archivo vacío
 * @param previous en las brechas entre sentencias, la sentencia anterior; si no, {@code null}
 * @param whitespace el espacio que hay ahora: el original, o el que dejó la regla anterior
 * @param shape dentro de cuántos bloques está y con qué indentación quedó la línea
 */
public record Gap(GapKind kind, Node node, Node previous, String whitespace, Shape shape) {

  /** {@code true} en el espacio antes de la primera sentencia del archivo. */
  public boolean atStart() {
    return kind == GapKind.FILE_START;
  }

  /** {@code true} en el espacio después de la última sentencia del archivo. */
  public boolean atEnd() {
    return kind == GapKind.FILE_END;
  }

  /** {@code true} si el espacio tiene al menos un salto de línea. */
  public boolean breaksLine() {
    return whitespace.indexOf('\n') >= 0;
  }

  /** {@code true} si lo que sigue empieza una línea: hay un salto de línea o es el principio. */
  public boolean startsLine() {
    return atStart() || breaksLine();
  }

  /** {@code true} si es un espacio entre dos partes de la misma línea. */
  public boolean insideLine() {
    return !atStart() && !atEnd() && !breaksLine();
  }

  /** Cantidad de saltos de línea del espacio. */
  public int lineBreaks() {
    return (int) whitespace.chars().filter(c -> c == '\n').count();
  }

  /** El mismo espacio con otro contenido. */
  public Gap withWhitespace(String newWhitespace) {
    return new Gap(kind, node, previous, newWhitespace, shape);
  }

  /**
   * Un salto de línea más {@code blankLines} líneas vacías, con la indentación de la línea actual.
   */
  public String newLine(int blankLines) {
    return "\n".repeat(blankLines + 1) + shape.lineIndent();
  }
}
