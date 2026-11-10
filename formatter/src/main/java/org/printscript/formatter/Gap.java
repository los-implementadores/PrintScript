package org.printscript.formatter;

import org.printscript.token.Token;
import org.printscript.token.TokenType;

/**
 * El espacio entre dos tokens consecutivos: lo único que cambian las reglas de formato.
 *
 * @param previous el token de antes, o {@code null} si es el espacio del principio del archivo
 * @param next el token de después; es {@code EOF} en el espacio del final del archivo
 * @param whitespace el espacio que hay ahora (el original, o el que dejó la regla anterior)
 * @param lineIndent la indentación de la línea en la que está {@code previous}, ya formateada
 */
public record Gap(Token previous, Token next, String whitespace, String lineIndent) {

  /** {@code true} en el espacio antes del primer token del archivo. */
  public boolean atStart() {
    return previous == null;
  }

  /** {@code true} en el espacio después del último token del archivo. */
  public boolean atEnd() {
    return next.getType() == TokenType.EOF;
  }

  /** {@code true} si el espacio tiene al menos un salto de línea. */
  public boolean breaksLine() {
    return whitespace.indexOf('\n') >= 0;
  }

  /** {@code true} si lo que sigue empieza una línea: hay un salto de línea o es el principio. */
  public boolean startsLine() {
    return atStart() || breaksLine();
  }

  /** {@code true} si es el espacio entre dos tokens de la misma línea. */
  public boolean insideLine() {
    return !atStart() && !atEnd() && !breaksLine();
  }

  /** Cantidad de saltos de línea del espacio. */
  public int lineBreaks() {
    return (int) whitespace.chars().filter(c -> c == '\n').count();
  }

  /** {@code true} si el token de antes es de tipo {@code type}. */
  public boolean isAfter(TokenType type) {
    return previous != null && previous.getType() == type;
  }

  /** {@code true} si el token de después es de tipo {@code type}. */
  public boolean isBefore(TokenType type) {
    return next.getType() == type;
  }

  /** {@code true} si el token de después es el identificador {@code name}. */
  public boolean isBeforeIdentifier(String name) {
    return isBefore(TokenType.IDENTIFIER) && next.getLexeme().equals(name);
  }

  /** El mismo espacio con otro contenido. */
  public Gap withWhitespace(String newWhitespace) {
    return new Gap(previous, next, newWhitespace, lineIndent);
  }

  /**
   * Un salto de línea más {@code blankLines} líneas vacías, con la indentación de la línea actual.
   */
  public String newLine(int blankLines) {
    return "\n".repeat(blankLines + 1) + lineIndent;
  }
}
