package org.printscript.analyzer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.printscript.analyzer.rule.LintRule;
import org.printscript.analyzer.rule.LintRules;
import org.printscript.analyzer.violation.Severity;
import org.printscript.analyzer.violation.Violation;
import org.printscript.ast.BinaryExpression;
import org.printscript.ast.CallExpression;
import org.printscript.ast.ExpressionStatement;
import org.printscript.ast.Identifier;
import org.printscript.ast.Node;
import org.printscript.ast.NumberLiteral;
import org.printscript.ast.VarDeclarationStatement;
import org.printscript.rules.RuleConfig;
import org.printscript.source.Position;

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
          public void onEnter(Node node, Consumer<Violation> report) {
            if (node instanceof VarDeclarationStatement declaration
                && "x".equals(declaration.getName().getName())) {
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
    StaticAnalyzer analyzer =
        new StaticAnalyzerImpl(RuleConfig.of(Map.of("activeUnusedVariables", true)));

    analyzer.analyze(declare("unused"));

    assertEquals(1, analyzer.getViolations().size());
    assertEquals(1, analyzer.getViolations().size());
  }

  @Test
  void reportsInTraversalOrderWithEndOfProgramRulesLast() {
    RuleConfig config =
        RuleConfig.of(
            Map.of(
                "identifier_format", "snake case",
                "activeComplexPrintln", true,
                "activeUnusedVariables", true));
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
    RuleConfig config = RuleConfig.of(Map.of("activeUnusedVariables", true));
    Supplier<List<LintRule>> rules = () -> LintRules.create(config);

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
    assertTrue(LintRules.create(RuleConfig.empty()).isEmpty());
  }
}
