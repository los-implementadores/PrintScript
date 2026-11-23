package org.printscript.analyzer.rule;

import java.util.List;
import java.util.Optional;
import org.printscript.rules.RuleConfig;
import org.printscript.rules.RuleRegistry;
import org.printscript.rules.RuleSetting;

/**
 * Las reglas activas del linter, separadas por lo que necesitan ver: un nodo o el programa entero.
 *
 * <p>Los registros de abajo son el único lugar donde se dice qué reglas existen y con qué clave se
 * activa cada una: agregar una regla es escribir su clase y sumar una línea en el registro que le
 * corresponde. Cada regla acepta su clave propia y, si existe, la del TCK.
 *
 * @param nodeRules las reglas que revisan un nodo por vez
 * @param programRules las reglas que necesitan el programa entero
 */
public record LintRules(List<NodeRule<?>> nodeRules, List<ProgramRule<?>> programRules) {

  private static final RuleRegistry<NodeRule<?>> NODE_RULES =
      RuleRegistry.<NodeRule<?>>builder()
          .rule("activeNamingConvention", "identifier_format")
          .with(LintRules::namingConvention)
          .rule("activeComplexPrintln", "mandatory-variable-or-literal-in-println")
          .whenOn(() -> new SimpleArgumentRule("println"))
          .rule("activeComplexReadInput", "mandatory-variable-or-literal-in-readInput")
          .whenOn(() -> new SimpleArgumentRule("readInput"))
          .build();

  private static final RuleRegistry<ProgramRule<?>> PROGRAM_RULES =
      RuleRegistry.<ProgramRule<?>>builder()
          .rule("activeUnusedVariables")
          .whenOn(UnusedVariableRule::new)
          .build();

  public LintRules {
    nodeRules = List.copyOf(nodeRules);
    programRules = List.copyOf(programRules);
  }

  /**
   * Las reglas que la configuración deja activas.
   *
   * @throws IllegalArgumentException si una clave tiene un valor que su regla no acepta
   */
  public static LintRules from(RuleConfig config) {
    return new LintRules(NODE_RULES.create(config), PROGRAM_RULES.create(config));
  }

  /** {@code true} si no hay ninguna regla activa. */
  public boolean isEmpty() {
    return nodeRules.isEmpty() && programRules.isEmpty();
  }

  /**
   * La regla de nombres se activa de dos formas: con {@code "activeNamingConvention": true} y la
   * convención en {@code namingConventionFormat} (camelCase si no está), o con la convención
   * directamente en {@code identifier_format}, como la configura el TCK.
   */
  private static Optional<NodeRule<?>> namingConvention(RuleSetting setting) {
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
