package org.printscript.common.ast;

import org.printscript.common.Position;

/** Literal numérico — p.ej. {@code 42} o {@code 3.14}. */
public final class NumberLiteral implements Expression {

  private final double value;
  private final Position position;

  public NumberLiteral(double value, Position position) {
    this.value = value;
    this.position = position;
  }

  public double getValue() {
    return value;
  }

  @Override
  public Position getPosition() {
    return position;
  }

  @Override
  public <T> T accept(ASTVisitor<T> visitor) {
    return visitor.visitNumberLiteral(this);
  }
}
