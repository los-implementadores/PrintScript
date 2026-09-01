package org.printscript.parser.parselet.prefix;

import java.util.ArrayList;
import java.util.List;
import org.printscript.common.ast.CallExpression;
import org.printscript.common.ast.Expression;
import org.printscript.common.ast.Identifier;
import org.printscript.common.token.Token;
import org.printscript.common.token.TokenType;
import org.printscript.parser.ParseContext;
import org.printscript.parser.TokenStream;
import org.printscript.parser.parselet.PrefixParselet;

/**
 * Parsea un identificador. Si a continuación hay un paréntesis de apertura, lo trata como una
 * llamada a función ({@code nombre(args)}); de lo contrario, es una referencia a variable.
 */
public final class IdentifierParselet implements PrefixParselet {

  @Override
  public Expression parse(Token token, ParseContext ctx) {
    TokenStream ts = ctx.tokens();
    if (ts.currentType() == TokenType.LPAREN) {
      return parseCall(token, ctx);
    }
    return new Identifier(token.getLexeme(), token.getPosition());
  }

  private Expression parseCall(Token nameToken, ParseContext ctx) {
    TokenStream ts = ctx.tokens();
    ts.consume(TokenType.LPAREN);

    List<Expression> args = new ArrayList<>();
    if (ts.currentType() != TokenType.RPAREN) {
      args.add(ctx.parseExpression(0));
    }

    Token close = ts.current();
    ts.consume(TokenType.RPAREN);
    return new CallExpression(
        nameToken.getLexeme(), args, ts.span(nameToken.getPosition(), close.getPosition()));
  }
}
