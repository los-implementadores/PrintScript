package org.printscript.ast;

import java.util.List;
import org.printscript.source.Position;

/**
 * Literal de cadena — p.ej. {@code "hello"} o {@code 'hello'}.
 *
 * <p>Guarda el valor (lo de adentro de las comillas, lo que usa el intérprete) y el lexema (el
 * literal tal como se escribió, con sus comillas, lo que escribe el formatter).
 */
public final class StringLiteral implements Expression {

  private final String value;
  private final String lexeme;
  private final Position position;

  /** Un literal con comillas dobles, o simples si el valor tiene comillas dobles. */
  public StringLiteral(String value, Position position) {
    this(value, quote(value), position);
  }

  /**
   * @param value el texto de adentro de las comillas
   * @param lexeme el literal tal como está en el código fuente, comillas incluidas
   */
  public StringLiteral(String value, String lexeme, Position position) {
    this.value = value;
    this.lexeme = lexeme;
    this.position = position;
  }

  private static String quote(String value) {
    String quote = value.indexOf('"') >= 0 ? "'" : "\"";
    return quote + value + quote;
  }

  public String getValue() {
    return value;
  }

  /** El literal tal como se escribió, comillas incluidas. */
  public String getLexeme() {
    return lexeme;
  }

  @Override
  public Position getPosition() {
    return position;
  }

  @Override
  public List<Node> children() {
    return List.of();
  }

  @Override
  public <T> T accept(ASTVisitor<T> visitor) {
    return visitor.visitStringLiteral(this);
  }
}
