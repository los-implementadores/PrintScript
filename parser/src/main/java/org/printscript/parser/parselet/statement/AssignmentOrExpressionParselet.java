package org.printscript.parser.parselet.statement;

import org.printscript.common.Position;
import org.printscript.common.ast.AssignmentStatement;
import org.printscript.common.ast.CallExpression;
import org.printscript.common.ast.Expression;
import org.printscript.common.ast.ExpressionStatement;
import org.printscript.common.ast.Identifier;
import org.printscript.common.ast.Statement;
import org.printscript.common.token.Token;
import org.printscript.common.token.TokenType;
import org.printscript.parser.ParseContext;
import org.printscript.parser.TokenStream;
import org.printscript.parser.parselet.StatementParselet;

/**
 * Parsea una sentencia que empieza con un identificador. Puede ser:
 *
 * <ul>
 *   <li>una asignación: {@code x = <expr> ;}
 *   <li>una llamada como sentencia: {@code foo(<args>) ;}
 *   <li>una expresión que empieza con el identificador: {@code x + 1 ;}
 * </ul>
 */
public final class AssignmentOrExpressionParselet implements StatementParselet {

  @Override
  public Statement parse(ParseContext ctx) {
    TokenStream ts = ctx.tokens();

    Position start = ts.current().getPosition();
    Token nameToken = ts.current();
    ts.advance();

    if (ts.currentType() == TokenType.ASSIGN) {
      ts.advance();
      Identifier target = new Identifier(nameToken.getLexeme(), nameToken.getPosition());
      Expression value = ctx.parseExpression(0);
      Token semi = ts.current();
      ts.consume(TokenType.SEMICOLON);
      return new AssignmentStatement(target, value, ts.span(start, semi.getPosition()));
    }

    if (ts.currentType() == TokenType.LPAREN) {
      CallExpression call = parseCall(nameToken, ctx);
      Token semi = ts.current();
      ts.consume(TokenType.SEMICOLON);
      return new ExpressionStatement(call, ts.span(start, semi.getPosition()));
    }

    Expression left = new Identifier(nameToken.getLexeme(), nameToken.getPosition());
    Expression expr = ctx.parseInfix(left, 0);
    Token semi = ts.current();
    ts.consume(TokenType.SEMICOLON);
    return new ExpressionStatement(expr, ts.span(start, semi.getPosition()));
  }

  private CallExpression parseCall(Token nameToken, ParseContext ctx) {
    TokenStream ts = ctx.tokens();
    ts.consume(TokenType.LPAREN);

    java.util.List<Expression> args = new java.util.ArrayList<>();
    if (ts.currentType() != TokenType.RPAREN) {
      args.add(ctx.parseExpression(0));
    }

    Token close = ts.current();
    ts.consume(TokenType.RPAREN);
    return new CallExpression(
        nameToken.getLexeme(), args, ts.span(nameToken.getPosition(), close.getPosition()));
  }
}
