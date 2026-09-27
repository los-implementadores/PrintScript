package org.printscript.runner;

import java.io.Reader;
import java.util.List;
import java.util.Map;
import org.printscript.common.token.TokenType;
import org.printscript.lexer.IdentifierTokenMatcher;
import org.printscript.lexer.Lexer;
import org.printscript.lexer.LexerImpl;
import org.printscript.lexer.NumberTokenMatcher;
import org.printscript.lexer.StringTokenMatcher;
import org.printscript.lexer.SymbolTokenMatcher;
import org.printscript.lexer.TokenMatcher;

/** Construye el {@link Lexer} con el set de keywords y símbolos de PrintScript. */
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

  public static Lexer create(Reader source) {
    List<TokenMatcher> matchers =
        List.of(
            new IdentifierTokenMatcher(KEYWORDS),
            new NumberTokenMatcher(),
            new StringTokenMatcher(),
            new SymbolTokenMatcher(SYMBOLS));
    return new LexerImpl(source, matchers);
  }
}
