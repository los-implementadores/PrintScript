package org.printscript.parser;

import java.util.Iterator;
import org.printscript.common.Position;
import org.printscript.common.token.Token;
import org.printscript.common.token.TokenType;

/**
 * Encapsula el consumo del stream de tokens: mantiene el token actual, avanza y valida.
 *
 * <p>Extraer esta responsabilidad de {@code ParserImpl} respeta SRP: el parser decide <em>qué</em>
 * construir, el {@code TokenStream} gestiona <em>cómo</em> se recorren los tokens.
 */
public final class TokenStream {

  private final Iterator<Token> tokens;
  private Token current;

  public TokenStream(Iterator<Token> tokens) {
    this.tokens = tokens;
    this.current = tokens.next(); // carga el primer token
  }

  /** Token actual, sin consumirlo. */
  public Token current() {
    return current;
  }

  /** Tipo del token actual. */
  public TokenType currentType() {
    return current.getType();
  }

  /** {@code true} mientras no se haya llegado al EOF. */
  public boolean atEnd() {
    return current.getType() == TokenType.EOF;
  }

  /** Avanza al siguiente token (si hay). Devuelve el token que estaba antes de avanzar. */
  public Token advance() {
    Token previous = current;
    if (tokens.hasNext()) {
      current = tokens.next();
    }
    return previous;
  }

  /**
   * Verifica que el token actual sea del tipo esperado y avanza. Fail-fast: lanza {@link
   * ParseException} con la posición del token inesperado.
   */
  public Token consume(TokenType expected) {
    if (current.getType() != expected) {
      throw new ParseException(
          "Expected " + expected + " but found '" + current.getLexeme() + "'",
          current.getPosition());
    }
    return advance();
  }

  /** Construye una posición que abarca desde {@code start} hasta {@code end}. */
  public Position span(Position start, Position end) {
    return new Position(
        start.getStartLine(), start.getStartColumn(), end.getEndLine(), end.getEndColumn());
  }
}
