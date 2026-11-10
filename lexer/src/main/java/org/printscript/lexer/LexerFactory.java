package org.printscript.lexer;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.printscript.token.TokenType;

/**
 * Arma el {@link Lexer} con el vocabulario de PrintScript: sus palabras reservadas y sus símbolos.
 *
 * <p>Qué palabras son reservadas y qué caracteres son símbolos es una decisión del lexer, así que
 * vive acá y en un solo lugar. La CLI, los adapters del TCK y los tests de otros módulos piden el
 * lexer a esta fábrica en vez de armar sus propios mapas.
 *
 * <p>El lexer no sabe qué construcciones acepta el lenguaje: reconoce todas las palabras, y qué se
 * puede escribir con ellas lo decide la gramática con la que se arma el parser.
 */
public final class LexerFactory {

  private static final Map<String, TokenType> KEYWORDS =
      Map.ofEntries(
          Map.entry("let", TokenType.LET),
          Map.entry("const", TokenType.CONST),
          Map.entry("if", TokenType.IF),
          Map.entry("else", TokenType.ELSE),
          Map.entry("number", TokenType.TYPE_NUMBER),
          Map.entry("string", TokenType.TYPE_STRING),
          Map.entry("boolean", TokenType.TYPE_BOOLEAN),
          Map.entry("true", TokenType.BOOLEAN_LITERAL),
          Map.entry("false", TokenType.BOOLEAN_LITERAL));

  private static final Map<Character, TokenType> SYMBOLS =
      Map.ofEntries(
          Map.entry(':', TokenType.COLON),
          Map.entry('=', TokenType.ASSIGN),
          Map.entry(';', TokenType.SEMICOLON),
          Map.entry('(', TokenType.LPAREN),
          Map.entry(')', TokenType.RPAREN),
          Map.entry('{', TokenType.LBRACE),
          Map.entry('}', TokenType.RBRACE),
          Map.entry('+', TokenType.PLUS),
          Map.entry('-', TokenType.MINUS),
          Map.entry('*', TokenType.STAR),
          Map.entry('/', TokenType.SLASH));

  private LexerFactory() {}

  /**
   * Crea un lexer de PrintScript. Descarta los espacios entre tokens: es el que usa el parser.
   *
   * @param source código fuente a tokenizar
   * @return un lexer con el vocabulario completo del lenguaje
   */
  public static Lexer create(Reader source) {
    return new LexerImpl(source, defaultMatchers());
  }

  /**
   * Crea un lexer de PrintScript que también devuelve los espacios entre tokens (como tokens {@code
   * WHITESPACE}). Concatenando los lexemas de todos sus tokens se obtiene el código fuente
   * original: es lo que necesita el formatter.
   *
   * @param source código fuente a tokenizar
   * @return un lexer con el vocabulario completo del lenguaje y los espacios
   */
  public static Lexer createWithWhitespace(Reader source) {
    List<TokenMatcher> matchers = new ArrayList<>();
    matchers.add(new WhitespaceTokenMatcher());
    matchers.addAll(defaultMatchers());
    return new LexerImpl(source, matchers);
  }

  /**
   * Devuelve los matchers de PrintScript en el orden en que se prueban. Sirve para armar un lexer
   * con matchers extra sin perder los del lenguaje.
   *
   * @return lista inmutable de matchers
   */
  public static List<TokenMatcher> defaultMatchers() {
    return List.of(
        new IdentifierTokenMatcher(KEYWORDS),
        new NumberTokenMatcher(),
        new StringTokenMatcher(),
        new SymbolTokenMatcher(SYMBOLS));
  }
}
