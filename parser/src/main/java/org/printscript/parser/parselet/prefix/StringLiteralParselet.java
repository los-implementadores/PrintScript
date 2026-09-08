package org.printscript.parser.parselet.prefix;

import org.printscript.common.ast.Expression;
import org.printscript.common.ast.StringLiteral;
import org.printscript.common.token.Token;
import org.printscript.parser.ParseContext;
import org.printscript.parser.parselet.PrefixParselet;

/** Parsea un literal de cadena. */
public final class StringLiteralParselet implements PrefixParselet {

  @Override
  public Expression parse(Token token, ParseContext ctx) {
    return new StringLiteral(token.getLexeme(), token.getPosition());
  }
}
