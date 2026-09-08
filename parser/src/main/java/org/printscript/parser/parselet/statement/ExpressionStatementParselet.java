package org.printscript.parser.parselet.statement;

import org.printscript.common.Position;
import org.printscript.common.ast.Expression;
import org.printscript.common.ast.ExpressionStatement;
import org.printscript.common.ast.Statement;
import org.printscript.common.token.Token;
import org.printscript.common.token.TokenType;
import org.printscript.parser.ParseContext;
import org.printscript.parser.TokenStream;
import org.printscript.parser.parselet.StatementParselet;

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
