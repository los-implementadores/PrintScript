package org.printscript.parser.parselet.prefix;

import org.printscript.ast.Expression;
import org.printscript.ast.GroupExpression;
import org.printscript.parser.ParseContext;
import org.printscript.parser.parselet.PrefixParselet;
import org.printscript.token.Token;
import org.printscript.token.TokenType;

/**
 * Parsea una expresión agrupada entre paréntesis: {@code ( expr )}. Los paréntesis quedan en el AST
 * como un {@link GroupExpression}, para que el formatter no los pierda (ver ADR-029).
 */
public final class GroupParselet implements PrefixParselet {

  @Override
  public Expression parse(Token token, ParseContext ctx) {
    Expression inner = ctx.parseExpression(0);
    Token close = ctx.tokens().consume(TokenType.RPAREN);
    return new GroupExpression(inner, ctx.tokens().span(token.getPosition(), close.getPosition()));
  }
}
