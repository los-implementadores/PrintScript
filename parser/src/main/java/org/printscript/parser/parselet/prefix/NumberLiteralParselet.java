package org.printscript.parser.parselet.prefix;

import org.printscript.common.ast.Expression;
import org.printscript.common.ast.NumberLiteral;
import org.printscript.common.token.Token;
import org.printscript.parser.ParseContext;
import org.printscript.parser.parselet.PrefixParselet;

/** Parsea un literal numérico. */
public final class NumberLiteralParselet implements PrefixParselet {

  @Override
  public Expression parse(Token token, ParseContext ctx) {
    double value = Double.parseDouble(token.getLexeme());
    return new NumberLiteral(value, token.getPosition());
  }
}
