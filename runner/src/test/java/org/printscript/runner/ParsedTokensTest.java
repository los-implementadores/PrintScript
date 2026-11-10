package org.printscript.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import org.printscript.parser.ParseException;
import org.printscript.token.Token;
import org.printscript.token.TokenType;

class ParsedTokensTest {

  private final TokenLoader loader = new ParsedTokenLoader();

  @Test
  void returnsEveryTokenOfTheSourceIncludingSpaces() {
    String source = "let x:  number = 1;\n\nprintln(x);\n";
    StringBuilder rebuilt = new StringBuilder();
    List<TokenType> types = new ArrayList<>();

    Iterator<Token> tokens = loader.load(new StringReader(source), LanguageVersion.V1_0);
    while (tokens.hasNext()) {
      Token token = tokens.next();
      rebuilt.append(token.getLexeme());
      types.add(token.getType());
    }

    assertEquals(source, rebuilt.toString());
    assertEquals(TokenType.EOF, types.get(types.size() - 1));
    assertThrows(NoSuchElementException.class, tokens::next);
  }

  @Test
  void aSyntaxErrorStopsTheTokensAtTheStatementThatHasIt() {
    Iterator<Token> tokens =
        loader.load(new StringReader("let a: number = 1;\nlet = ;"), LanguageVersion.V1_0);

    // La primera sentencia se puede leer entera antes de que el parser llegue al error.
    StringBuilder first = new StringBuilder();
    for (int i = 0; i < 11; i++) {
      first.append(tokens.next().getLexeme());
    }
    assertEquals("let a: number = 1;", first.toString());
    assertThrows(
        ParseException.class,
        () -> {
          while (tokens.hasNext()) {
            tokens.next();
          }
        });
  }
}
