package org.printscript.analyzer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.printscript.analyzer.TestAst.POS;
import static org.printscript.analyzer.TestAst.call;
import static org.printscript.analyzer.TestAst.declaration;
import static org.printscript.analyzer.TestAst.sum;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.printscript.analyzer.rule.LintRules;
import org.printscript.analyzer.violation.Violation;
import org.printscript.rules.RuleConfig;
import org.printscript.rules.RuleDefinition;
import org.printscript.rules.RuleValue;

/**
 * Lo que el linter dice de sus reglas a quien no conoce sus clases: qué regla encontró cada
 * violación, qué reglas existen y cuáles se usan por defecto.
 */
class LintRuleCatalogTest {

  @Test
  void eachViolationSaysWhichRuleFoundIt() {
    Map<String, Object> config = new HashMap<>();
    config.put("identifier_format", "camel case");
    config.put("mandatory-variable-or-literal-in-println", true);
    config.put("activeComplexReadInput", true);
    config.put("activeUnusedVariables", true);

    List<Violation> violations =
        LintRulesTest.analyze(
            config,
            declaration("my_variable", POS),
            call("println", sum()),
            call("readInput", sum()));

    assertEquals(
        List.of(
            "activeNamingConvention",
            "activeComplexPrintln",
            "activeComplexReadInput",
            "activeUnusedVariables"),
        violations.stream().map(Violation::rule).sorted(LintRuleCatalogTest::byRuleOrder).toList());
  }

  @Test
  void describesEveryRuleWithItsKeyAliasesAndValue() {
    List<RuleDefinition> definitions = LintRules.definitions();

    assertEquals(
        List.of(
            "activeNamingConvention",
            "activeComplexPrintln",
            "activeComplexReadInput",
            "activeUnusedVariables"),
        definitions.stream().map(RuleDefinition::key).toList());
    RuleDefinition naming = definitions.get(0);
    assertEquals(List.of("identifier_format"), naming.aliases());
    assertEquals(RuleValue.FLAG, naming.value());
    assertEquals(
        List.of(new RuleDefinition.Parameter("namingConventionFormat", RuleValue.TEXT)),
        naming.parameters());
    assertTrue(definitions.stream().allMatch(rule -> rule.value() == RuleValue.FLAG));
  }

  @Test
  void theDefaultsTurnOnEveryRuleWithCamelCase() {
    RuleConfig defaults = LintRules.defaults();
    LintRules rules = LintRules.from(defaults);

    assertEquals(3, rules.nodeRules().size());
    assertEquals(1, rules.programRules().size());
    assertEquals("camelCase", defaults.text("namingConventionFormat", "-"));
  }

  private static int byRuleOrder(String a, String b) {
    List<String> order = LintRules.definitions().stream().map(RuleDefinition::key).toList();
    return Integer.compare(order.indexOf(a), order.indexOf(b));
  }
}
