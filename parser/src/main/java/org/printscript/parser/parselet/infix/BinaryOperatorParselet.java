package org.printscript.parser.parselet.infix;

import org.printscript.common.ast.BinaryExpression;
import org.printscript.common.ast.Expression;
import org.printscript.common.token.Token;
import org.printscript.parser.ParseContext;
import org.printscript.parser.parselet.InfixParselet;

/**
 * Parsea un operador binario (aritmético) entre dos expresiones. Es asociativo a izquierda: para el
 * operando derecho recurre con el mismo binding power, de modo que {@code 1 + 2 + 3} agrupa como
 * {@code (1 + 2) + 3}.
 */
public final class BinaryOperatorParselet implements InfixParselet {

  private final int bindingPower;

  public BinaryOperatorParselet(int bindingPower) {
    this.bindingPower = bindingPower;
  }

  @Override
  public int bindingPower() {
    return bindingPower;
  }

  @Override
  public Expression parse(Expression left, Token token, ParseContext ctx) {
    Expression right = ctx.parseExpression(bindingPower);
    return new BinaryExpression(
        left, token.getLexeme(), right, ctx.tokens().span(left.getPosition(), right.getPosition()));
  }
}
