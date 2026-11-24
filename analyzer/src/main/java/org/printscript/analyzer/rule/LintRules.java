package org.printscript.analyzer.rule;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import org.printscript.rules.RuleConfig;
import org.printscript.rules.RuleDefinition;
import org.printscript.rules.RuleRegistry;
import org.printscript.rules.RuleSetting;
import org.printscript.rules.RuleValue;

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
public record LintRules(List<NodeRule<?>> nodeRules, List<ProgramRule> programRules) {

  /** La clave del TCK para la regla de nombres: el valor es la convención. */
  private static final String IDENTIFIER_FORMAT = "identifier_format";

  /** La convención de nombres que pide {@code activeNamingConvention}: camelCase o snake_case. */
  private static final String NAMING_CONVENTION_FORMAT = "namingConventionFormat";

  private static final RuleRegistry<NodeRule<?>> NODE_RULES =
      RuleRegistry.<NodeRule<?>>builder()
          .rule(NamingConventionRule.KEY, IDENTIFIER_FORMAT)
          .parameter(NAMING_CONVENTION_FORMAT, RuleValue.TEXT)
          .with(RuleValue.FLAG, LintRules::namingConvention)
          .rule(SimpleArgumentRule.PRINTLN_KEY, "mandatory-variable-or-literal-in-println")
          .whenOn(SimpleArgumentRule::println)
          .rule(SimpleArgumentRule.READ_INPUT_KEY, "mandatory-variable-or-literal-in-readInput")
          .whenOn(SimpleArgumentRule::readInput)
          .build();

  private static final RuleRegistry<ProgramRule> PROGRAM_RULES =
      RuleRegistry.<ProgramRule>builder()
          .rule(UnusedVariableRule.KEY)
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

  /**
   * Las reglas que existen, con la clave y el valor de cada una: las que revisan un nodo y después
   * las que necesitan el programa entero.
   */
  public static List<RuleDefinition> definitions() {
    return Stream.concat(NODE_RULES.definitions().stream(), PROGRAM_RULES.definitions().stream())
        .toList();
  }

  /**
   * Las reglas que se usan cuando no se configura ninguna: todas prendidas, con nombres en
   * camelCase. Es la misma configuración que {@code linter_rules.json}, más la regla de {@code
   * readInput}, que es de la 1.1.
   */
  public static RuleConfig defaults() {
    Map<String, Object> defaults = new LinkedHashMap<>();
    defaults.put(NamingConventionRule.KEY, true);
    defaults.put(NAMING_CONVENTION_FORMAT, "camelCase");
    defaults.put(SimpleArgumentRule.PRINTLN_KEY, true);
    defaults.put(SimpleArgumentRule.READ_INPUT_KEY, true);
    defaults.put(UnusedVariableRule.KEY, true);
    return RuleConfig.of(defaults);
  }

  /** {@code true} si no hay ninguna regla activa. */
  public boolean isEmpty() {
    return nodeRules.isEmpty() && programRules.isEmpty();
  }

  /**
   * La regla de nombres se activa de dos formas: con la convención directamente en {@code
   * identifier_format}, como la configura el TCK, o con {@code "activeNamingConvention": true} y la
   * convención en {@code namingConventionFormat} (camelCase si no está). Con {@code null}, ninguna
   * de las dos la activa.
   */
  private static Optional<NodeRule<?>> namingConvention(RuleSetting setting) {
    if (setting.isNull()) {
      return Optional.empty();
    }
    return NAMING_CONVENTION_BY_KEY.get(setting.key()).apply(setting);
  }

  /** Cómo arma la regla de nombres cada una de sus dos claves. */
  private static final Map<String, Function<RuleSetting, Optional<NodeRule<?>>>>
      NAMING_CONVENTION_BY_KEY =
          Map.of(
              IDENTIFIER_FORMAT,
              LintRules::namingConventionFromFormat,
              NamingConventionRule.KEY,
              LintRules::namingConventionFromActiveFlag);

  /** Con {@code identifier_format}, el valor es la convención. */
  private static Optional<NodeRule<?>> namingConventionFromFormat(RuleSetting setting) {
    return Optional.of(namingConventionRule(setting.asText()));
  }

  /**
   * Con {@code activeNamingConvention}, la regla se arma solo con {@code true}, y la convención
   * viene en {@code namingConventionFormat}.
   */
  private static Optional<NodeRule<?>> namingConventionFromActiveFlag(RuleSetting setting) {
    if (!setting.asBoolean()) {
      return Optional.empty();
    }
    String convention = setting.config().text(NAMING_CONVENTION_FORMAT, "camelCase");
    return Optional.of(namingConventionRule(convention));
  }

  private static NodeRule<?> namingConventionRule(String convention) {
    return new NamingConventionRule(NamingConvention.fromString(convention));
  }
}
