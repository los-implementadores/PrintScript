package org.printscript.analyzer.rule;

import java.util.List;
import java.util.Optional;
import org.printscript.rules.RuleConfig;
import org.printscript.rules.RuleRegistry;
import org.printscript.rules.RuleSetting;

/**
 * Las reglas del linter y la clave con la que se activa cada una. Es el único lugar donde se
 * registran: agregar una regla es escribir su clase y sumar una línea acá. Cada regla acepta su
 * clave propia y, si existe, la del TCK.
 *
 * <p>Las reglas se aplican (y reportan) en el orden en que están registradas.
 */
public final class LintRules {

  private static final RuleRegistry<LintRule> REGISTRY =
      RuleRegistry.<LintRule>builder()
          .rule("activeNamingConvention", "identifier_format")
          .with(LintRules::namingConvention)
          .rule("activeComplexPrintln", "mandatory-variable-or-literal-in-println")
          .whenOn(() -> new SimpleArgumentRule("println"))
          .rule("activeComplexReadInput", "mandatory-variable-or-literal-in-readInput")
          .whenOn(() -> new SimpleArgumentRule("readInput"))
          .rule("activeUnusedVariables")
          .whenOn(UnusedVariableRule::new)
          .build();

  private LintRules() {}

  /**
   * Arma reglas nuevas con las que la configuración deja activas.
   *
   * @throws IllegalArgumentException si una clave tiene un valor que su regla no acepta
   */
  public static List<LintRule> create(RuleConfig config) {
    return REGISTRY.create(config);
  }

  /**
   * La regla de nombres se activa de dos formas: con {@code "activeNamingConvention": true} y la
   * convención en {@code namingConventionFormat} (camelCase si no está), o con la convención
   * directamente en {@code identifier_format}, como la configura el TCK.
   */
  private static Optional<LintRule> namingConvention(RuleSetting setting) {
    if (setting.isNull()) {
      return Optional.empty();
    }
    if ("identifier_format".equals(setting.key())) {
      return Optional.of(new NamingConventionRule(NamingConvention.fromString(setting.asText())));
    }
    if (!setting.asBoolean()) {
      return Optional.empty();
    }
    String format = setting.config().text("namingConventionFormat", "camelCase");
    return Optional.of(new NamingConventionRule(NamingConvention.fromString(format)));
  }
}
