package org.printscript.analyzer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.printscript.analyzer.config.AnalyzerConfig;
import org.printscript.analyzer.config.NamingConvention;
import org.printscript.analyzer.rule.LintRule;
import org.printscript.analyzer.rule.LintRules;
import org.printscript.analyzer.violation.Severity;
import org.printscript.analyzer.violation.Violation;
import org.printscript.common.Position;
import org.printscript.common.ast.BinaryExpression;
import org.printscript.common.ast.CallExpression;
import org.printscript.common.ast.ExpressionStatement;
import org.printscript.common.ast.Identifier;
import org.printscript.common.ast.NumberLiteral;
import org.printscript.common.ast.VarDeclarationStatement;

class LintRuleTest {

  private static final Position POS = new Position(1, 1, 1, 10);

  private static VarDeclarationStatement declare(String name) {
    return new VarDeclarationStatement(
        new Identifier(name, POS), "number", new NumberLiteral(1, POS), POS);
  }

  private static ExpressionStatement printSum(String name) {
    BinaryExpression sum =
        new BinaryExpression(new Identifier(name, POS), "+", new NumberLiteral(1, POS), POS);
    return new ExpressionStatement(new CallExpression("println", List.of(sum), POS), POS);
  }

  @Test
  void customRuleRunsWithoutModifyingTheAnalyzer() {
    LintRule forbidX =
        new LintRule() {
          @Override
          public void onDeclaration(VarDeclarationStatement node, Consumer<Violation> report) {
            if ("x".equals(node.getName().getName())) {
              report.accept(new Violation("Do not call it x.", Severity.ERROR, node.getPosition()));
            }
          }
        };
    StaticAnalyzer analyzer = new StaticAnalyzerImpl(() -> List.of(forbidX));

    analyzer.analyze(declare("x"));
    analyzer.analyze(declare("y"));

    List<Violation> violations = analyzer.getViolations();
    assertEquals(1, violations.size());
    assertEquals("Do not call it x.", violations.get(0).getMessage());
  }

  @Test
  void violationsCanBeRequestedTwiceWithoutDuplicates() {
    AnalyzerConfig config = new AnalyzerConfig();
    config.activeUnusedVariables = true;
    StaticAnalyzer analyzer = new StaticAnalyzerImpl(config);

    analyzer.analyze(declare("unused"));

    assertEquals(1, analyzer.getViolations().size());
    assertEquals(1, analyzer.getViolations().size());
  }

  @Test
  void reportsInTraversalOrderWithEndOfProgramRulesLast() {
    AnalyzerConfig config = new AnalyzerConfig();
    config.activeNamingConvention = true;
    config.namingConventionFormat = NamingConvention.SNAKE_CASE;
    config.activeComplexPrintln = true;
    config.activeUnusedVariables = true;
    StaticAnalyzer analyzer = new StaticAnalyzerImpl(config);

    analyzer.analyze(declare("myVar"));
    analyzer.analyze(declare("other"));
    analyzer.analyze(printSum("myVar"));

    List<String> messages = analyzer.getViolations().stream().map(Violation::getMessage).toList();
    assertEquals(
        List.of(
            "Variable 'myVar' should be in snake_case.",
            "Complex expression in 'println'. Consider extracting to an intermediate variable.",
            "Variable 'other' is declared but never used."),
        messages);
  }

  @Test
  void analyzersFromTheSameFactoryDoNotShareState() {
    AnalyzerConfig config = new AnalyzerConfig();
    config.activeUnusedVariables = true;
    Supplier<List<LintRule>> rules = () -> LintRules.from(config);

    StaticAnalyzer first = new StaticAnalyzerImpl(rules);
    first.analyze(declare("onlyInFirst"));
    StaticAnalyzer second = new StaticAnalyzerImpl(rules);
    second.analyze(declare("inSecond"));
    second.analyze(printSum("inSecond"));

    assertTrue(second.getViolations().isEmpty());
    assertEquals(1, first.getViolations().size());
  }

  @Test
  void emptyConfigActivatesNoRules() {
    assertTrue(LintRules.from(new AnalyzerConfig()).isEmpty());
  }
}
