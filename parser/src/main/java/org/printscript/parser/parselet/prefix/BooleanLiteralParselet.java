package org.printscript.parser.parselet.prefix;

import org.printscript.common.ast.BooleanLiteral;
import org.printscript.common.ast.Expression;
import org.printscript.common.token.Token;
import org.printscript.parser.ParseContext;
import org.printscript.parser.parselet.PrefixParselet;

/** Parsea un literal booleano {@code true} / {@code false} (PrintScript 1.1). */
public final class BooleanLiteralParselet implements PrefixParselet {

  @Override
  public Expression parse(Token token, ParseContext ctx) {
    boolean value = Boolean.parseBoolean(token.getLexeme());
    return new BooleanLiteral(value, token.getPosition());
  }
}
