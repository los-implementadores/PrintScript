package org.printscript.analyzer.rule;

import java.util.ArrayList;
import java.util.List;
import org.printscript.analyzer.config.AnalyzerConfig;

/**
 * Único lugar donde se registran las reglas configurables del linter, igual que {@code
 * ParserGrammar} para los parselets.
 */
public final class LintRules {

  private LintRules() {}

  /**
   * Arma las reglas que la configuración deja activas, en el orden en que se aplican.
   *
   * @param config configuración del linter
   * @return reglas activas
   */
  public static List<LintRule> from(AnalyzerConfig config) {
    List<LintRule> rules = new ArrayList<>();
    if (config.activeNamingConvention) {
      rules.add(new NamingConventionRule(config.namingConventionFormat));
    }
    if (config.activeComplexPrintln) {
      rules.add(new SimpleArgumentRule("println"));
    }
    if (config.activeComplexReadInput) {
      rules.add(new SimpleArgumentRule("readInput"));
    }
    if (config.activeUnusedVariables) {
      rules.add(new UnusedVariableRule());
    }
    return rules;
  }
}
