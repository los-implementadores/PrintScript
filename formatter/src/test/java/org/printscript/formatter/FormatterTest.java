package org.printscript.formatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.printscript.formatter.Formatting.format;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.printscript.ast.CallExpression;
import org.printscript.ast.ExpressionStatement;
import org.printscript.ast.Identifier;
import org.printscript.ast.Statement;
import org.printscript.rules.RuleConfig;
import org.printscript.source.Position;
import org.printscript.source.SourceText;

/** El motor del formatter: escribe el AST como estaba y le pasa cada brecha a las reglas. */
class FormatterTest {

  private static final Formatter NO_RULES = new FormatterImpl(RuleConfig.empty());

  @Test
  void withoutRulesTheSourceIsUntouched() {
    String source =
        "  let  x :\tnumber=(1.50+ 2) ;\r\n\n  if(x){println( 'a = \"b\"' );}else{ }\n"
            + "const y: boolean = true;\nx = readInput( );";

    assertEquals(source, format(NO_RULES, source));
  }

  @Test
  void anEmptyProgramKeepsItsWhitespace() {
    assertEquals("", format(NO_RULES, ""));
    assertEquals(" \n ", format(NO_RULES, " \n "));
  }

  @Test
  void eachGapSaysWhatItIs() {
    List<String> gaps = new ArrayList<>();
    FormattingRule record =
        gap -> {
          gaps.add(gap.kind() + "@" + gap.shape().depth());
          return gap.whitespace();
        };

    format(new FormatterImpl(() -> List.of(record)), "let x: number = (1 + 2);\nif (x) { y; }");

    assertEquals(
        List.of(
            "FILE_START@0",
            "AFTER_KEYWORD@0",
            "BEFORE_TYPE_COLON@0",
            "AFTER_TYPE_COLON@0",
            "BEFORE_ASSIGN@0",
            "AFTER_ASSIGN@0",
            "INSIDE_PARENS@0",
            "BEFORE_OPERATOR@0",
            "AFTER_OPERATOR@0",
            "INSIDE_PARENS@0",
            "BEFORE_SEMICOLON@0",
            "BETWEEN_STATEMENTS@0",
            "AFTER_KEYWORD@0",
            "INSIDE_PARENS@0",
            "INSIDE_PARENS@0",
            "BEFORE_BLOCK@0",
            "BLOCK_START@1",
            "BEFORE_SEMICOLON@1",
            "BLOCK_END@0",
            "FILE_END@0"),
        gaps);
  }

  @Test
  void aGapBetweenStatementsKnowsBothStatements() {
    List<String> seen = new ArrayList<>();
    FormattingRule record =
        gap -> {
          if (gap.kind() == GapKind.BETWEEN_STATEMENTS) {
            seen.add(
                gap.previous().getClass().getSimpleName()
                    + "->"
                    + gap.node().getClass().getSimpleName());
          }
          return gap.whitespace();
        };

    format(new FormatterImpl(() -> List.of(record)), "let x: number = 1;\nprintln(x);");

    assertEquals(List.of("VarDeclarationStatement->ExpressionStatement"), seen);
  }

  @Test
  void aCustomRuleRunsWithoutChangingTheFormatter() {
    FormattingRule spaceInsideParens =
        gap -> gap.kind() == GapKind.INSIDE_PARENS ? " " : gap.whitespace();
    Formatter formatter = new FormatterImpl(() -> List.of(spaceInsideParens));

    assertEquals("println( ( x ) );", format(formatter, "println((x));"));
  }

  @Test
  void eachRuleSeesWhatThePreviousOneLeft() {
    FormattingRule addSpace = gap -> gap.insideLine() ? gap.whitespace() + " " : gap.whitespace();
    Formatter formatter = new FormatterImpl(() -> List.of(addSpace, addSpace));

    assertEquals("a  =  1  ;", format(formatter, "a=1;"));
  }

  @Test
  void eachFormatStartsWithFreshRules() {
    AtomicInteger created = new AtomicInteger();
    Formatter formatter =
        new FormatterImpl(
            () -> {
              created.incrementAndGet();
              return List.of();
            });

    format(formatter, "a;");
    format(formatter, "a;");

    assertEquals(2, created.get());
  }

  @Test
  void parenthesesAndLiteralsAreWrittenAsTheyWere() {
    assertEquals(
        "let r: number = (1 + 2) * 3.0;",
        format(
            new FormatterImpl(RuleConfig.of(Map.of("spaceAroundOperators", true))),
            "let r: number = (1+2)*3.0;"));
    assertEquals(
        "println( 'dijo  \"hola\"' );",
        format(
            new FormatterImpl(RuleConfig.of(Map.of("maxOneSpaceBetweenTokens", true))),
            "println(   'dijo  \"hola\"'   );"));
  }

  @Test
  void argumentsAreSeparatedByCommas() {
    Statement call =
        new ExpressionStatement(
            new CallExpression(
                "f", List.of(new Identifier("a", at(3)), new Identifier("b", at(6))), at(1)),
            at(1));
    FormattingRule tight = gap -> gap.insideLine() ? "" : gap.whitespace();
    StringWriter target = new StringWriter();

    new FormatterImpl(() -> List.of(tight))
        .format(List.of(call).iterator(), SourceText.of("f(a , b);"), target);

    assertEquals("f(a,b);", target.toString());
  }

  @Test
  void anAstThatDoesNotMatchTheSourceFails() {
    Statement other = new ExpressionStatement(new Identifier("b", at(1)), at(1));

    assertThrows(
        IllegalStateException.class,
        () -> NO_RULES.format(List.of(other).iterator(), SourceText.of("a;"), new StringWriter()));
    assertThrows(
        IllegalStateException.class,
        () ->
            NO_RULES.format(
                List.<Statement>of().iterator(), SourceText.of("a;"), new StringWriter()));
  }

  @Test
  void anInvalidValueFailsBeforeWritingAnything() {
    Formatter formatter = new FormatterImpl(RuleConfig.of(Map.of("indentSize", "four")));
    StringWriter target = new StringWriter();
    SourceText source = SourceText.of("a;");

    assertThrows(
        IllegalArgumentException.class,
        () -> formatter.format(Formatting.parse(source), source, target));
    assertEquals("", target.toString());
  }

  @Test
  void aWriteErrorIsReportedAsUnchecked() {
    Writer broken =
        new Writer() {
          @Override
          public void write(char[] buffer, int offset, int length) throws IOException {
            throw new IOException("disco lleno");
          }

          @Override
          public void flush() {}

          @Override
          public void close() {}
        };
    SourceText source = SourceText.of("a;");

    assertThrows(
        UncheckedIOException.class,
        () -> NO_RULES.format(Formatting.parse(source), source, broken));
  }

  private static Position at(int column) {
    return new Position(1, column, 1, column);
  }
}
