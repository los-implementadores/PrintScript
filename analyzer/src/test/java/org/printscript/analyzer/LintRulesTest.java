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
import org.printscript.analyzer.violation.Violation;
import org.printscript.ast.CallExpression;
import org.printscript.ast.GroupExpression;
import org.printscript.ast.Identifier;
import org.printscript.ast.NumberLiteral;
import org.printscript.ast.Statement;
import org.printscript.ast.StringLiteral;
import org.printscript.ast.VarDeclarationStatement;
import org.printscript.rules.RuleConfig;
import org.printscript.source.Position;

/** Las reglas del linter, activadas con las claves del archivo de configuración. */
class LintRulesTest {

  static List<Violation> analyze(Map<String, Object> config, Statement... statements) {
    return new StaticAnalyzerImpl(RuleConfig.of(config)).analyze(List.of(statements).iterator());
  }

  // --- Formato de nombres ---

  @Test
  void reportsAVariableThatIsNotInCamelCase() {
    List<Violation> violations =
        analyze(Map.of("activeNamingConvention", true), declaration("my_variable", POS));

    assertEquals(1, violations.size());
    assertEquals("Variable 'my_variable' should be in camelCase.", violations.get(0).message());
  }

  @Test
  void marksTheNameNotTheWholeDeclaration() {
    Position name = new Position(1, 5, 1, 15);
    Statement declaration =
        new VarDeclarationStatement(
            new Identifier("my_variable", name),
            "number",
            new NumberLiteral(1, POS),
            new Position(1, 1, 1, 30));

    List<Violation> violations = analyze(Map.of("activeNamingConvention", true), declaration);

    assertEquals(name, violations.get(0).position());
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

    assertEquals("Variable 'myVariable' should be in snake_case.", violations.get(0).message());
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
    assertTrue(violations.get(0).message().contains("Complex expression in 'println'"));
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
    assertTrue(violations.get(0).message().contains("readInput"));
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

    List<Violation> violations =
        analyzer.analyze(List.of(declaration("myVar", POS), call("println", sum())).iterator());

    assertEquals(2, violations.size());
  }

  @Test
  void findsAComplexArgumentInsideAnInitializer() {
    Statement readInput =
        declaration(
            "input",
            new CallExpression("readInput", List.of(sum()), POS),
            new Position(1, 1, 1, 40));

    assertEquals(
        1, analyze(Map.of("mandatory-variable-or-literal-in-readInput", true), readInput).size());
  }

  @Test
  void parenthesesAroundAVariableDoNotMakeItComplex() {
    Statement println =
        call(
            "println",
            new GroupExpression(new GroupExpression(new Identifier("x", POS), POS), POS));

    assertTrue(analyze(Map.of("activeComplexPrintln", true), println).isEmpty());
  }

  @Test
  void aViolationPrintsAsAWarningWithItsPosition() {
    Position pos = new Position(1, 5, 1, 15);
    Violation violation =
        new Violation("activeNamingConvention", "Variable should be camelCase", pos);

    assertEquals("[WARNING] Variable should be camelCase at 1:5-1:15", violation.toString());
  }
}
