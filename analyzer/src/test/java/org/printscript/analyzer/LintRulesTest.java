package org.printscript.analyzer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.printscript.analyzer.TestAst.POS;
import static org.printscript.analyzer.TestAst.call;
import static org.printscript.analyzer.TestAst.declaration;
import static org.printscript.analyzer.TestAst.ifElse;
import static org.printscript.analyzer.TestAst.println;
import static org.printscript.analyzer.TestAst.sum;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.printscript.analyzer.violation.Severity;
import org.printscript.analyzer.violation.Violation;
import org.printscript.ast.Statement;
import org.printscript.ast.StringLiteral;
import org.printscript.rules.RuleConfig;
import org.printscript.source.Position;

/** Las reglas del linter, activadas con las claves del archivo de configuración. */
class LintRulesTest {

  static List<Violation> analyze(Map<String, Object> config, Statement... statements) {
    StaticAnalyzer analyzer = new StaticAnalyzerImpl(RuleConfig.of(config));
    for (Statement statement : statements) {
      analyzer.analyze(statement);
    }
    return analyzer.getViolations();
  }

  // --- Formato de nombres ---

  @Test
  void reportsAVariableThatIsNotInCamelCase() {
    List<Violation> violations =
        analyze(Map.of("activeNamingConvention", true), declaration("my_variable", POS));

    assertEquals(1, violations.size());
    assertEquals("Variable 'my_variable' should be in camelCase.", violations.get(0).getMessage());
    assertEquals(Severity.WARNING, violations.get(0).getSeverity());
  }

  @Test
  void acceptsSnakeCaseWhenThatIsTheConfiguredConvention() {
    Map<String, Object> config =
        Map.of("activeNamingConvention", true, "namingConventionFormat", "snake_case");

    assertTrue(analyze(config, declaration("my_variable", POS)).isEmpty());
  }

  @Test
  void theTckKeyActivatesTheRuleWithItsConvention() {
    List<Violation> violations =
        analyze(Map.of("identifier_format", "snake case"), declaration("myVariable", POS));

    assertEquals("Variable 'myVariable' should be in snake_case.", violations.get(0).getMessage());
  }

  @Test
  void aNullFormatKeepsCamelCase() {
    Map<String, Object> config = new HashMap<>();
    config.put("activeNamingConvention", true);
    config.put("namingConventionFormat", null);

    assertEquals(1, analyze(config, declaration("my_variable", POS)).size());
  }

  @Test
  void anUnknownConventionIsAConfigurationError() {
    assertThrows(
        IllegalArgumentException.class,
        () -> analyze(Map.of("identifier_format", "kebab case"), declaration("x", POS)));
  }

  @Test
  void turningTheRuleOffOrGivingItNullLeavesItOff() {
    Map<String, Object> nullFormat = new HashMap<>();
    nullFormat.put("identifier_format", null);

    assertTrue(
        analyze(Map.of("activeNamingConvention", false), declaration("my_var", POS)).isEmpty());
    assertTrue(analyze(nullFormat, declaration("my_var", POS)).isEmpty());
  }

  // --- Argumento simple en println y readInput ---

  @Test
  void reportsAnExpressionPassedToPrintln() {
    List<Violation> violations =
        analyze(Map.of("mandatory-variable-or-literal-in-println", true), call("println", sum()));

    assertEquals(1, violations.size());
    assertTrue(violations.get(0).getMessage().contains("Complex expression in 'println'"));
  }

  @Test
  void acceptsALiteralOrIdentifierPassedToPrintln() {
    Map<String, Object> config = Map.of("activeComplexPrintln", true);

    assertTrue(analyze(config, call("println", new StringLiteral("Hola", POS))).isEmpty());
    assertTrue(analyze(config, println("x")).isEmpty());
  }

  @Test
  void reportsAnExpressionPassedToReadInputOnlyWhenItsRuleIsOn() {
    Statement readInput = call("readInput", sum());

    List<Violation> violations =
        analyze(Map.of("mandatory-variable-or-literal-in-readInput", true), readInput);
    assertEquals(1, violations.size());
    assertTrue(violations.get(0).getMessage().contains("readInput"));
    assertTrue(analyze(Map.of("activeComplexPrintln", true), readInput).isEmpty());
  }

  @Test
  void findsCallsNestedInsideOtherStatements() {
    Statement nested =
        ifElse(List.of(declaration("x", POS), call("println", sum())), List.of(println("x")));

    assertEquals(1, analyze(Map.of("activeComplexPrintln", true), nested).size());
  }

  // --- Configuración ---

  @Test
  void anEmptyConfigActivatesNoRules() {
    assertTrue(analyze(Map.of(), declaration("my_var", POS), call("println", sum())).isEmpty());
  }

  @Test
  void readsTheRulesFromAJsonFile() {
    String json =
        """
        {
          "identifier_format": "snake case",
          "mandatory-variable-or-literal-in-println": true,
          "activeUnusedVariables": false
        }
        """;
    StaticAnalyzer analyzer = new StaticAnalyzerImpl(RuleConfig.fromJson(new StringReader(json)));

    analyzer.analyze(declaration("myVar", POS));
    analyzer.analyze(call("println", sum()));

    assertEquals(2, analyzer.getViolations().size());
  }

  @Test
  void aViolationPrintsSeverityMessageAndPosition() {
    Position pos = new Position(1, 5, 1, 15);
    Violation violation = new Violation("Variable should be camelCase", Severity.WARNING, pos);

    assertEquals("Variable should be camelCase", violation.getMessage());
    assertEquals(Severity.WARNING, violation.getSeverity());
    assertEquals(pos, violation.getPosition());
    assertEquals("[WARNING] Variable should be camelCase at 1:5-1:15", violation.toString());
  }
}
