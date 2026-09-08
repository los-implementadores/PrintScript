package org.printscript.parser.parselet.statement;

import java.util.ArrayList;
import java.util.List;
import org.printscript.common.Position;
import org.printscript.common.ast.Expression;
import org.printscript.common.ast.IfStatement;
import org.printscript.common.ast.Statement;
import org.printscript.common.token.TokenType;
import org.printscript.parser.ParseContext;
import org.printscript.parser.ParseException;
import org.printscript.parser.TokenStream;
import org.printscript.parser.parselet.StatementParselet;

/**
 * Parsea un condicional {@code if (cond) { ... } else { ... }} (PrintScript 1.1).
 *
 * <ul>
 *   <li>Condición entre paréntesis.
 *   <li>Bloques siempre entre llaves.
 *   <li>{@code else} opcional. No se soporta {@code else if}: si tras {@code else} no aparece una
 *       llave de apertura, se reporta error.
 * </ul>
 *
 * <p>La validación de que la condición sea {@code boolean} se delega al análisis semántico.
 */
public final class IfStatementParselet implements StatementParselet {

  @Override
  public Statement parse(ParseContext ctx) {
    TokenStream ts = ctx.tokens();

    Position start = ts.current().getPosition();
    ts.consume(TokenType.IF);
    ts.consume(TokenType.LPAREN);
    Expression condition = ctx.parseExpression(0);
    ts.consume(TokenType.RPAREN);

    List<Statement> thenBlock = parseBlock(ctx);

    List<Statement> elseBlock = null;
    Position end = ts.current().getPosition();
    if (ts.currentType() == TokenType.ELSE) {
      ts.consume(TokenType.ELSE);
      if (ts.currentType() != TokenType.LBRACE) {
        throw new ParseException(
            "'else if' is not supported; 'else' must be followed by a block '{'",
            ts.current().getPosition());
      }
      elseBlock = parseBlock(ctx);
      end = ts.current().getPosition();
    }

    return new IfStatement(condition, thenBlock, elseBlock, ts.span(start, end));
  }

  /** Consume {@code { stmt* }} y devuelve la lista de sentencias del bloque. */
  private List<Statement> parseBlock(ParseContext ctx) {
    TokenStream ts = ctx.tokens();
    ts.consume(TokenType.LBRACE);
    List<Statement> statements = new ArrayList<>();
    while (ts.currentType() != TokenType.RBRACE) {
      if (ts.atEnd()) {
        throw new ParseException("Unterminated block: expected '}'", ts.current().getPosition());
      }
      statements.add(ctx.parseStatement());
    }
    ts.consume(TokenType.RBRACE);
    return statements;
  }
}
