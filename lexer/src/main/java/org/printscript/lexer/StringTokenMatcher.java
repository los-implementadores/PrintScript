package org.printscript.lexer;

import org.printscript.source.Position;
import org.printscript.token.Token;
import org.printscript.token.TokenType;

/**
 * Matcher para literales de tipo string, delimitados por {@code "} o {@code '}.
 *
 * <p>El lexema incluye las comillas, tal como está en el código fuente: el formatter lo reescribe
 * sin cambios y el parser es quien saca el valor de adentro.
 *
 * <p>Lanza {@link LexerException} si el string no se cierra antes de llegar al final del archivo.
 */
public class StringTokenMatcher implements TokenMatcher {

  @Override
  public boolean matches(char currentChar) {
    return currentChar == '"' || currentChar == '\'';
  }

  @Override
  public Token extract(LexerContext context) {
    int startLine = context.getCurrentLine();
    int startColumn = context.getCurrentColumn();
    char quote = (char) context.getCurrentChar();

    StringBuilder sb = new StringBuilder();
    sb.append(quote);
    context.advance(); // consume la comilla de apertura

    while (context.getCurrentChar() != -1 && context.getCurrentChar() != quote) {
      sb.append((char) context.getCurrentChar());
      context.advance();
    }

    if (context.getCurrentChar() == -1) {
      Position position =
          new Position(
              startLine, startColumn, context.getCurrentLine(), context.getCurrentColumn() - 1);
      throw new LexerException("Unterminated string literal", position);
    }

    int endLine = context.getCurrentLine();
    int endColumn = context.getCurrentColumn();
    sb.append(quote);
    context.advance(); // consume la comilla de cierre

    return context.createToken(
        TokenType.STRING_LITERAL, sb.toString(), startLine, startColumn, endLine, endColumn);
  }
}
