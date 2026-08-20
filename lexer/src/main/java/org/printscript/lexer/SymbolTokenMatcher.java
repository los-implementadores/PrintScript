package org.printscript.lexer;

import java.util.Collections;
import java.util.Map;
import org.printscript.common.token.Token;
import org.printscript.common.token.TokenType;

/**
 * Matcher para símbolos y operadores de un solo caracter.
 *
 * <p>Reconoce cualquier caracter que esté registrado en el mapa de símbolos. Para agregar un nuevo
 * operador, basta con agregarlo al mapa — sin crear una clase nueva ni modificar esta.
 */
public class SymbolTokenMatcher implements TokenMatcher {

  private final Map<Character, TokenType> symbols;

  /**
   * @param symbols mapa de caracter → TokenType para operadores y puntuación
   */
  public SymbolTokenMatcher(Map<Character, TokenType> symbols) {
    this.symbols = Collections.unmodifiableMap(symbols);
  }

  @Override
  public boolean matches(char currentChar) {
    return symbols.containsKey(currentChar);
  }

  @Override
  public Token extract(LexerContext context) {
    int startLine = context.getCurrentLine();
    int startColumn = context.getCurrentColumn();
    char c = (char) context.getCurrentChar();

    TokenType type = symbols.get(c);
    context.advance();

    return context.createToken(
        type, String.valueOf(c), startLine, startColumn, startLine, startColumn);
  }
}
