package org.printscript.lexer;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.NoSuchElementException;
import org.printscript.common.Position;
import org.printscript.common.token.Token;
import org.printscript.common.token.TokenType;

/**
 * Implementación de {@link Lexer} basada en Strategy pattern.
 *
 * <p>Recibe una lista de {@link TokenMatcher} por constructor. Para producir un token, itera los
 * matchers hasta que uno reconoce el caracter actual y lo extrae. Esto cumple el Open/Closed
 * Principle: para soportar nuevos tipos de tokens, se agrega un matcher nuevo sin modificar esta
 * clase.
 *
 * <p>Implementa también {@link LexerContext} para exponer su estado interno a los matchers de forma
 * controlada.
 */
public class LexerImpl implements Lexer, LexerContext {

  private final List<TokenMatcher> matchers;
  private final Reader reader;

  private int currentChar;
  private int currentLine = 1;
  private int currentColumn = 1;
  private boolean finished = false;

  /**
   * @param reader fuente de caracteres a tokenizar
   * @param matchers lista ordenada de estrategias para reconocer tokens
   */
  public LexerImpl(Reader reader, List<TokenMatcher> matchers) {
    this.reader = reader;
    this.matchers = matchers;
    this.currentChar = rawRead();
  }

  // ---------------------------------------------------------------- Lexer (Iterator<Token>)

  @Override
  public boolean hasNext() {
    return !finished;
  }

  @Override
  public Token next() {
    if (finished) {
      throw new NoSuchElementException("No more tokens");
    }

    skipWhitespace();

    int startLine = currentLine;
    int startColumn = currentColumn;

    if (currentChar == -1) {
      finished = true;
      return createToken(TokenType.EOF, "", startLine, startColumn, startLine, startColumn);
    }

    char c = (char) currentChar;

    for (TokenMatcher matcher : matchers) {
      if (matcher.matches(c)) {
        return matcher.extract(this);
      }
    }

    Position position = new Position(startLine, startColumn, startLine, startColumn);
    throw new LexerException("Unexpected character '" + c + "'", position);
  }

  // ---------------------------------------------------------------- LexerContext

  @Override
  public int getCurrentChar() {
    return currentChar;
  }

  @Override
  public int getCurrentLine() {
    return currentLine;
  }

  @Override
  public int getCurrentColumn() {
    return currentColumn;
  }

  @Override
  public void advance() {
    if (currentChar == '\n') {
      currentLine++;
      currentColumn = 1;
    } else {
      currentColumn++;
    }
    currentChar = rawRead();
  }

  @Override
  public Token createToken(
      TokenType type, String lexeme, int startLine, int startColumn, int endLine, int endColumn) {
    return new Token(type, lexeme, new Position(startLine, startColumn, endLine, endColumn));
  }

  // ---------------------------------------------------------------- Internos

  private void skipWhitespace() {
    while (currentChar == ' '
        || currentChar == '\t'
        || currentChar == '\r'
        || currentChar == '\n') {
      advance();
    }
  }

  private int rawRead() {
    try {
      return reader.read();
    } catch (IOException e) {
      throw new LexerException(
          "I/O error reading source: " + e.getMessage(),
          new Position(currentLine, currentColumn, currentLine, currentColumn));
    }
  }
}
