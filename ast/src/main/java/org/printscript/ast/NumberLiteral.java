package org.printscript.ast;

import java.util.List;
import org.printscript.source.Position;

/**
 * Literal numérico — p.ej. {@code 42} o {@code 3.14}.
 *
 * <p>Guarda el valor (lo que usa el intérprete) y el lexema (el número tal como se escribió, lo que
 * escribe el formatter): {@code 5.0} y {@code 1.50} valen lo mismo que {@code 5} y {@code 1.5},
 * pero no se escriben igual.
 */
public final class NumberLiteral implements Expression {

  private final double value;
  private final String lexeme;
  private final Position position;

  /** Un literal escrito sin decimales si es entero ({@code 5}) y con ellos si no ({@code 1.5}). */
  public NumberLiteral(double value, Position position) {
    this(value, value % 1 == 0 ? String.valueOf((long) value) : String.valueOf(value), position);
  }

  /**
   * @param value el valor del número
   * @param lexeme el número tal como está en el código fuente
   */
  public NumberLiteral(double value, String lexeme, Position position) {
    this.value = value;
    this.lexeme = lexeme;
    this.position = position;
  }

  public double getValue() {
    return value;
  }

  /** El número tal como se escribió. */
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
    return visitor.visitNumberLiteral(this);
  }
}
