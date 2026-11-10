package org.printscript.formatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.printscript.formatter.rule.BlockIndentation;
import org.printscript.lexer.LexerFactory;
import org.printscript.rules.RuleConfig;
import org.printscript.source.Position;
import org.printscript.token.Token;
import org.printscript.token.TokenType;

/** El motor del formatter: reescribe los tokens y le pasa cada espacio a las reglas. */
class FormatterTest {

  private static String format(Formatter formatter, String source) {
    StringWriter target = new StringWriter();
    formatter.format(LexerFactory.createWithWhitespace(new StringReader(source)), target);
    return target.toString();
  }

  @Test
  void withoutRulesTheSourceIsUntouched() {
    String source = "let  x :\tnumber='a = b';\r\n\n  if(x){println( x );}\n";

    assertEquals(source, format(new FormatterImpl(RuleConfig.empty()), source));
  }

  @Test
  void aCustomRuleRunsWithoutChangingTheFormatter() {
    FormattingRule spaceAfterParen =
        gap -> gap.isAfter(TokenType.LPAREN) && gap.insideLine() ? " " : gap.whitespace();
    Formatter formatter = new FormatterImpl(() -> List.of(spaceAfterParen));

    assertEquals("println( x);", format(formatter, "println(x);"));
  }

  @Test
  void eachRuleSeesWhatThePreviousOneLeft() {
    FormattingRule addSpace = gap -> gap.insideLine() ? gap.whitespace() + " " : gap.whitespace();
    Formatter formatter = new FormatterImpl(() -> List.of(addSpace, addSpace));

    assertEquals("a  =  1  ;", format(formatter, "a=1;"));
  }

  @Test
  void eachFormatStartsWithFreshRules() {
    Supplier<List<FormattingRule>> rules = () -> List.of(new BlockIndentation(2));
    Formatter formatter = new FormatterImpl(rules);
    String unclosed = "if (a) {\nx;";

    assertEquals("if (a) {\n  x;", format(formatter, unclosed));
    // Si la regla fuera la misma, arrancaría con la profundidad del formateo anterior.
    assertEquals("if (a) {\n  x;", format(formatter, unclosed));
  }

  @Test
  void whitespaceAfterTheLastTokenIsKeptWhenThereIsNoEof() {
    Position pos = new Position(1, 1, 1, 1);
    Iterator<Token> tokens =
        List.of(
                new Token(TokenType.IDENTIFIER, "a", pos),
                new Token(TokenType.WHITESPACE, "  \n", pos))
            .iterator();
    StringWriter target = new StringWriter();

    new FormatterImpl(RuleConfig.empty()).format(tokens, target);

    assertEquals("a  \n", target.toString());
  }

  @Test
  void anInvalidValueFailsBeforeWritingAnything() {
    Formatter formatter = new FormatterImpl(RuleConfig.of(Map.of("indentSize", "four")));
    StringWriter target = new StringWriter();

    assertThrows(
        IllegalArgumentException.class,
        () -> formatter.format(LexerFactory.createWithWhitespace(new StringReader("a;")), target));
    assertEquals("", target.toString());
  }

  @Test
  void aWriteErrorIsReportedAsUnchecked() {
    Writer broken =
        new Writer() {
          @Override
          public void write(char[] buffer, int offset, int length) throws java.io.IOException {
            throw new java.io.IOException("disco lleno");
          }

          @Override
          public void flush() {}

          @Override
          public void close() {}
        };

    assertThrows(
        UncheckedIOException.class,
        () ->
            new FormatterImpl(RuleConfig.empty())
                .format(LexerFactory.createWithWhitespace(new StringReader("a;")), broken));
  }
}
