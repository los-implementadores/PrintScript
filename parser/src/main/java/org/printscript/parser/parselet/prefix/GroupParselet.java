package org.printscript.parser.parselet.prefix;

import org.printscript.common.ast.Expression;
import org.printscript.common.token.Token;
import org.printscript.common.token.TokenType;
import org.printscript.parser.ParseContext;
import org.printscript.parser.parselet.PrefixParselet;

/**
 * Parsea una expresión agrupada entre paréntesis: {@code ( expr )}. Los paréntesis solo agrupan; no
 * producen un nodo propio en el AST, devuelven la expresión interna.
 */
public final class GroupParselet implements PrefixParselet {

  @Override
  public Expression parse(Token token, ParseContext ctx) {
    Expression inner = ctx.parseExpression(0);
    ctx.tokens().consume(TokenType.RPAREN);
    return inner;
  }
}
