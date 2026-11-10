package org.printscript.runner;

import java.io.Reader;
import java.util.Iterator;
import org.printscript.lexer.LexerFactory;
import org.printscript.parser.ParserImpl;
import org.printscript.token.Token;

/** Los tokens con espacios, validados con el parser de la versión (ver {@link ParsedTokens}). */
final class ParsedTokenLoader implements TokenLoader {

  @Override
  public Iterator<Token> load(Reader source, LanguageVersion version) {
    return new ParsedTokens(
        LexerFactory.createWithWhitespace(source),
        tokens -> new ParserImpl(tokens, version.grammar()));
  }
}
