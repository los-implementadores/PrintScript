package org.printscript.parser.parselet.prefix;

import org.printscript.ast.Expression;
import org.printscript.ast.NumberLiteral;
import org.printscript.parser.ParseContext;
import org.printscript.parser.parselet.PrefixParselet;
import org.printscript.token.Token;

/** Parsea un literal numérico. */
public final class NumberLiteralParselet implements PrefixParselet {

  @Override
  public Expression parse(Token token, ParseContext ctx) {
    double value = Double.parseDouble(token.getLexeme());
    return new NumberLiteral(value, token.getPosition());
  }
}
