package org.printscript.parser.parselet.statement;

import org.printscript.ast.Expression;
import org.printscript.ast.Identifier;
import org.printscript.ast.Statement;
import org.printscript.ast.VarDeclarationStatement;
import org.printscript.parser.ParseContext;
import org.printscript.parser.ParseException;
import org.printscript.parser.TokenStream;
import org.printscript.parser.parselet.StatementParselet;
import org.printscript.source.Position;
import org.printscript.token.Token;
import org.printscript.token.TokenType;

/**
 * Parsea una declaración de variable: {@code let <ident> : <type> = <expr> ;}.
 *
 * <p>Qué tipos se admiten lo define la gramática ({@link ParseContext#parseTypeName}), no esta
 * clase.
 */
public final class VarDeclarationParselet implements StatementParselet {

  private final TokenType keyword;
  private final boolean isConst;

  /**
   * Declaración con la keyword indicada. {@code isConst=true} marca la variable como inmutable
   * (como {@code const}): tiene que tener valor y no se puede reasignar.
   */
  public VarDeclarationParselet(TokenType keyword, boolean isConst) {
    this.keyword = keyword;
    this.isConst = isConst;
  }

  @Override
  public Statement parse(ParseContext ctx) {
    TokenStream ts = ctx.tokens();

    Position start = ts.current().getPosition();
    ts.consume(keyword);

    Token nameToken = ts.current();
    ts.consume(TokenType.IDENTIFIER);
    Identifier name = new Identifier(nameToken.getLexeme(), nameToken.getPosition());

    ts.consume(TokenType.COLON);
    String typeName = ctx.parseTypeName();

    Expression initializer = null;
    if (ts.current().getType() == TokenType.ASSIGN) {
      ts.consume(TokenType.ASSIGN);
      initializer = ctx.parseExpression(0);
    } else if (isConst) {
      throw new ParseException(
          "Constant declaration must have an initializer", ts.current().getPosition());
    }

    Token semi = ts.current();
    ts.consume(TokenType.SEMICOLON);

    return new VarDeclarationStatement(
        name, typeName, initializer, isConst, ts.span(start, semi.getPosition()));
  }
}
