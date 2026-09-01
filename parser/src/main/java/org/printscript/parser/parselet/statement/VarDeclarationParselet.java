package org.printscript.parser.parselet.statement;

import org.printscript.common.Position;
import org.printscript.common.ast.Expression;
import org.printscript.common.ast.Identifier;
import org.printscript.common.ast.Statement;
import org.printscript.common.ast.VarDeclarationStatement;
import org.printscript.common.token.Token;
import org.printscript.common.token.TokenType;
import org.printscript.parser.ParseContext;
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

  public VarDeclarationParselet(TypeNameParser typeNameParser) {
    this.typeNameParser = typeNameParser;
  }

  @Override
  public Statement parse(ParseContext ctx) {
    TokenStream ts = ctx.tokens();

    Position start = ts.current().getPosition();
    ts.consume(TokenType.LET);

    Token nameToken = ts.current();
    ts.consume(TokenType.IDENTIFIER);
    Identifier name = new Identifier(nameToken.getLexeme(), nameToken.getPosition());

    ts.consume(TokenType.COLON);
    String typeName = typeNameParser.parse(ts);

    ts.consume(TokenType.ASSIGN);
    Expression initializer = ctx.parseExpression(0);

    Token semi = ts.current();
    ts.consume(TokenType.SEMICOLON);

    return new VarDeclarationStatement(
        name, typeName, initializer, ts.span(start, semi.getPosition()));
  }
}
