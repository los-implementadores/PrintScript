package org.printscript.parser.parselet.statement;

import org.printscript.common.Position;
import org.printscript.common.ast.Expression;
import org.printscript.common.ast.Identifier;
import org.printscript.common.ast.Statement;
import org.printscript.common.ast.VarDeclarationStatement;
import org.printscript.common.token.Token;
import org.printscript.common.token.TokenType;
import org.printscript.parser.ParseContext;
import org.printscript.parser.ParseException;
import org.printscript.parser.TokenStream;
import org.printscript.parser.TypeNameParser;
import org.printscript.parser.parselet.StatementParselet;

/**
 * Parsea una declaración de variable: {@code let <ident> : <type> = <expr> ;}.
 *
 * <p>Delega la validación del nombre de tipo en un {@link TypeNameParser} inyectado, para no
 * hardcodear los tipos admitidos.
 */
public final class VarDeclarationParselet implements StatementParselet {

  private final TypeNameParser typeNameParser;
  private final TokenType keyword;
  private final boolean isConst;

  /** Declaración mutable con {@code let}. */
  public VarDeclarationParselet(TypeNameParser typeNameParser) {
    this(typeNameParser, TokenType.LET, false);
  }

  /**
   * Declaración con la keyword indicada. {@code isConst=true} marca la variable como inmutable
   * (usado para {@code const}, PrintScript 1.1).
   */
  public VarDeclarationParselet(TypeNameParser typeNameParser, TokenType keyword, boolean isConst) {
    this.typeNameParser = typeNameParser;
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
    String typeName = typeNameParser.parse(ts);

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
