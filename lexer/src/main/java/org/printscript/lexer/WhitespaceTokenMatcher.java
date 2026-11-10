package org.printscript.lexer;

import org.printscript.token.Token;
import org.printscript.token.TokenType;

/**
 * Matcher para los espacios, tabs y saltos de línea entre tokens. Produce un único token {@code
 * WHITESPACE} con todo el tramo, tal como está en el código fuente.
 *
 * <p>Solo lo usa el lexer que arma {@link LexerFactory#createWithWhitespace}: el formatter necesita
 * los espacios originales para dejar intactos los que ninguna regla toca. El lexer normal no lo
 * tiene, y los espacios que ningún matcher reconoce se descartan.
 */
public class WhitespaceTokenMatcher implements TokenMatcher {

  @Override
  public boolean matches(char currentChar) {
    return isWhitespace(currentChar);
  }

  @Override
  public Token extract(LexerContext context) {
    int startLine = context.getCurrentLine();
    int startColumn = context.getCurrentColumn();

    StringBuilder sb = new StringBuilder();
    int endLine = startLine;
    int endColumn = startColumn;
    while (context.getCurrentChar() != -1 && isWhitespace((char) context.getCurrentChar())) {
      endLine = context.getCurrentLine();
      endColumn = context.getCurrentColumn();
      sb.append((char) context.getCurrentChar());
      context.advance();
    }

    return context.createToken(
        TokenType.WHITESPACE, sb.toString(), startLine, startColumn, endLine, endColumn);
  }

  /** Los caracteres que el lenguaje trata como espacio entre tokens. */
  static boolean isWhitespace(char c) {
    return c == ' ' || c == '\t' || c == '\r' || c == '\n';
  }
}
