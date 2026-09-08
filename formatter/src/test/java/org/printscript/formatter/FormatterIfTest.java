package org.printscript.formatter;

import static org.junit.jupiter.api.Assertions.*;

import java.io.StringReader;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.printscript.common.Position;
import org.printscript.common.ast.*;

/** Tests de #36: formateo de bloques {@code if}/{@code else} con indentación configurable. */
class FormatterIfTest {

  private static final Position POS = new Position(1, 1, 1, 1);

  private IfStatement sampleIf(boolean withElse) {
    Statement thenStmt =
        new ExpressionStatement(
            new CallExpression("println", List.of(new StringLiteral("a", POS)), POS), POS);
    List<Statement> elseBlock =
        withElse
            ? List.of(
                new ExpressionStatement(
                    new CallExpression("println", List.of(new StringLiteral("b", POS)), POS), POS))
            : null;
    return new IfStatement(new BooleanLiteral(true, POS), List.of(thenStmt), elseBlock, POS);
  }

  private String format(Statement stmt, FormattingRules rules) {
    return stmt.accept(new FormatterVisitor(rules));
  }

  @Test
  void opensBraceOnSameLineWithDefaultIndent() {
    String out = format(sampleIf(false), new FormattingRules());
    assertEquals("if (true) {\n    println(\"a\");\n}", out);
  }

  @Test
  void formatsIfElse() {
    String out = format(sampleIf(true), new FormattingRules());
    assertEquals("if (true) {\n    println(\"a\");\n} else {\n    println(\"b\");\n}", out);
  }

  @Test
  void indentSizeIsConfigurable() {
    FormattingRules rules = FormattingRules.fromJson(new StringReader("{\"indentSize\": 2}"));
    String out = format(sampleIf(false), rules);
    assertEquals("if (true) {\n  println(\"a\");\n}", out);
  }
}
