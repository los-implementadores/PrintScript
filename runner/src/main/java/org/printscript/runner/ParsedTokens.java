package org.printscript.runner;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;
import org.printscript.ast.Statement;
import org.printscript.token.Token;
import org.printscript.token.TokenType;

/**
 * Los tokens del código fuente (espacios incluidos) para el formatter, validados por el parser.
 *
 * <p>Cada vez que el formatter se queda sin tokens, el parser lee la sentencia siguiente: así un
 * error de sintaxis frena el formateo y nunca hay más de una sentencia en memoria. El parser no ve
 * los espacios; los tokens que lee se guardan, espacios incluidos, hasta que el formatter los pide.
 */
final class ParsedTokens implements Iterator<Token> {

  private final Iterator<Token> source;
  private final Deque<Token> read = new ArrayDeque<>();
  private final Iterator<Statement> statements;

  /**
   * @param source los tokens con espacios ({@code LexerFactory.createWithWhitespace})
   * @param parser arma el parser sobre los tokens sin espacios
   */
  ParsedTokens(Iterator<Token> source, Function<Iterator<Token>, Iterator<Statement>> parser) {
    this.source = source;
    this.statements = parser.apply(new WithoutWhitespace());
  }

  @Override
  public boolean hasNext() {
    while (read.isEmpty() && statements.hasNext()) {
      statements.next();
    }
    return !read.isEmpty();
  }

  @Override
  public Token next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    return read.poll();
  }

  /** Lo que ve el parser: los tokens sin los espacios. Guarda todos los que lee. */
  private final class WithoutWhitespace implements Iterator<Token> {

    @Override
    public boolean hasNext() {
      // El lexer siempre termina con EOF: si queda algo, queda al menos un token que no es espacio.
      return source.hasNext();
    }

    @Override
    public Token next() {
      Token token;
      do {
        token = source.next();
        read.add(token);
      } while (token.getType() == TokenType.WHITESPACE);
      return token;
    }
  }
}
