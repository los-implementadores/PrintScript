package org.printscript.parser.parselet.statement;

import org.printscript.ast.Expression;
import org.printscript.ast.ExpressionStatement;
import org.printscript.ast.Statement;
import org.printscript.parser.ParseContext;
import org.printscript.parser.TokenStream;
import org.printscript.parser.parselet.StatementParselet;
import org.printscript.source.Position;
import org.printscript.token.Token;
import org.printscript.token.TokenType;

/**
 * Parsea una expresión usada como sentencia: {@code <expr> ;}. Es el caso por defecto cuando ningún
 * otro {@link StatementParselet} aplica al token inicial.
 */
public final class ExpressionStatementParselet implements StatementParselet {

  @Override
  public Statement parse(ParseContext ctx) {
    TokenStream ts = ctx.tokens();
    Position start = ts.current().getPosition();
    Expression expr = ctx.parseExpression(0);
    Token semi = ts.current();
    ts.consume(TokenType.SEMICOLON);
    return new ExpressionStatement(expr, ts.span(start, semi.getPosition()));
  }
}
