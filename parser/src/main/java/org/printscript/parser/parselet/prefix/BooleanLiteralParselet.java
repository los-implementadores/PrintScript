package org.printscript.parser.parselet.prefix;

import org.printscript.ast.BooleanLiteral;
import org.printscript.ast.Expression;
import org.printscript.parser.ParseContext;
import org.printscript.parser.parselet.PrefixParselet;
import org.printscript.token.Token;

/** Parsea un literal booleano {@code true} / {@code false} */
public final class BooleanLiteralParselet implements PrefixParselet {

  @Override
  public Expression parse(Token token, ParseContext ctx) {
    boolean value = Boolean.parseBoolean(token.getLexeme());
    return new BooleanLiteral(value, token.getPosition());
  }
}
