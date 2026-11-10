package org.printscript.parser.parselet.prefix;

import org.printscript.ast.Expression;
import org.printscript.ast.StringLiteral;
import org.printscript.parser.ParseContext;
import org.printscript.parser.parselet.PrefixParselet;
import org.printscript.token.Token;

/** Parsea un literal de cadena. */
public final class StringLiteralParselet implements PrefixParselet {

  @Override
  public Expression parse(Token token, ParseContext ctx) {
    return new StringLiteral(token.getLexeme(), token.getPosition());
  }
}
