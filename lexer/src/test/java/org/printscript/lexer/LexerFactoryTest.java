package org.printscript.lexer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.printscript.common.token.TokenType;

class LexerFactoryTest {

  private List<TokenType> types(String source) {
    Lexer lexer = LexerFactory.create(new StringReader(source));
    List<TokenType> types = new ArrayList<>();
    while (lexer.hasNext()) {
      types.add(lexer.next().getType());
    }
    return types;
  }

  @Test
  void recognizesKeywordsOfEveryVersion() {
    assertEquals(
        List.of(
            TokenType.LET,
            TokenType.CONST,
            TokenType.IF,
            TokenType.ELSE,
            TokenType.TYPE_NUMBER,
            TokenType.TYPE_STRING,
            TokenType.TYPE_BOOLEAN,
            TokenType.BOOLEAN_LITERAL,
            TokenType.BOOLEAN_LITERAL,
            TokenType.EOF),
        types("let const if else number string boolean true false"));
  }

  @Test
  void recognizesEverySymbol() {
    assertEquals(
        List.of(
            TokenType.COLON,
            TokenType.ASSIGN,
            TokenType.SEMICOLON,
            TokenType.LPAREN,
            TokenType.RPAREN,
            TokenType.LBRACE,
            TokenType.RBRACE,
            TokenType.PLUS,
            TokenType.MINUS,
            TokenType.STAR,
            TokenType.SLASH,
            TokenType.EOF),
        types(": = ; ( ) { } + - * /"));
  }

  @Test
  void builtInFunctionsAreIdentifiers() {
    assertEquals(
        List.of(TokenType.IDENTIFIER, TokenType.IDENTIFIER, TokenType.IDENTIFIER, TokenType.EOF),
        types("println readInput readEnv"));
  }
}
