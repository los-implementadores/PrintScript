package org.printscript.lexer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.printscript.source.Position;
import org.printscript.token.Token;
import org.printscript.token.TokenType;

/** El lexer que usa el formatter: devuelve también los espacios entre tokens. */
class WhitespaceTokensTest {

  private List<Token> tokenize(String source) {
    List<Token> tokens = new ArrayList<>();
    LexerFactory.createWithWhitespace(new StringReader(source)).forEachRemaining(tokens::add);
    return tokens;
  }

  @Test
  void theLexemesRebuildTheSourceExactly() {
    String source = "let  x :\tnumber='a b';\r\n\n  println( x );\n";

    StringBuilder rebuilt = new StringBuilder();
    tokenize(source).forEach(token -> rebuilt.append(token.getLexeme()));

    assertEquals(source, rebuilt.toString());
  }

  @Test
  void aRunOfSpacesAndLineBreaksIsASingleToken() {
    List<Token> tokens = tokenize("a; \n\t b;");

    assertEquals(TokenType.WHITESPACE, tokens.get(2).getType());
    assertEquals(" \n\t ", tokens.get(2).getLexeme());
    assertEquals(new Position(1, 3, 2, 2), tokens.get(2).getPosition());
    assertEquals(TokenType.IDENTIFIER, tokens.get(3).getType());
  }

  @Test
  void theParserLexerSkipsSpacesWithoutChangingPositions() {
    List<Token> tokens = new ArrayList<>();
    LexerFactory.create(new StringReader("  a\n b")).forEachRemaining(tokens::add);

    assertEquals(3, tokens.size());
    assertEquals(new Position(1, 3, 1, 3), tokens.get(0).getPosition());
    assertEquals(new Position(2, 2, 2, 2), tokens.get(1).getPosition());
    assertEquals(TokenType.EOF, tokens.get(2).getType());
  }
}
