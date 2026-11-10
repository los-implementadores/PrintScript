package org.printscript.formatter.rule;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.printscript.formatter.FormattingRule;
import org.printscript.rules.RuleConfig;
import org.printscript.rules.RuleRegistry;
import org.printscript.token.TokenType;

/**
 * Las reglas del formatter y la clave con la que se activa cada una. Es el único lugar donde se
 * registran: agregar una regla es escribir su clase y sumar una línea acá.
 *
 * <p>Las claves en camelCase son las propias; las otras, las del TCK. Con una clave propia, {@code
 * false} pide lo contrario ({@code "spaceAroundAssign": false} deja el {@code =} sin espacios). Con
 * una del TCK, {@code false} deja la regla apagada. Una regla que no está en la configuración no
 * toca el código.
 *
 * <p>Las reglas se aplican en el orden en que están registradas: primero las que deciden dónde se
 * corta cada línea, después los espacios dentro de una línea y al final la indentación, que corrige
 * el comienzo de todas las líneas.
 */
public final class FormattingRules {

  private static final Set<TokenType> OPERATORS =
      Set.of(TokenType.PLUS, TokenType.MINUS, TokenType.STAR, TokenType.SLASH);

  private static final RuleRegistry<FormattingRule> REGISTRY =
      RuleRegistry.<FormattingRule>builder()
          // Dónde se corta cada línea
          .rule("mandatory-line-break-after-statement")
          .whenOn(LineBreakAfterStatement::new)
          .rule("if-brace-same-line")
          .whenOn(() -> new BracePlacement(true))
          .rule("if-brace-below-line")
          .whenOn(() -> new BracePlacement(false))
          .rule("newlineBeforePrintln")
          .withInt(BlankLinesBeforePrintln::new)
          .rule("line-breaks-after-println")
          .withInt(BlankLinesAfterPrintln::new)
          // Espacios dentro de una línea
          .rule("maxOneSpaceBetweenTokens")
          .whenOn(MaxOneSpace::new)
          .rule("mandatory-single-space-separation")
          .whenOn(SingleSpaceSeparation::new)
          .rule("spaceBeforeColon")
          .withBoolean(on -> TokenSpacing.before(TokenType.COLON, on))
          .rule("enforce-spacing-before-colon-in-declaration")
          .whenOn(() -> TokenSpacing.before(TokenType.COLON, true))
          .rule("spaceAfterColon")
          .withBoolean(on -> TokenSpacing.after(TokenType.COLON, on))
          .rule("enforce-spacing-after-colon-in-declaration")
          .whenOn(() -> TokenSpacing.after(TokenType.COLON, true))
          .rule("spaceAroundAssign")
          .withBoolean(on -> TokenSpacing.around(Set.of(TokenType.ASSIGN), on))
          .rule("enforce-no-spacing-around-equals")
          .whenOn(() -> TokenSpacing.around(Set.of(TokenType.ASSIGN), false))
          .rule("enforce-spacing-around-equals")
          .whenOn(() -> TokenSpacing.around(Set.of(TokenType.ASSIGN), true))
          .rule("spaceAroundOperators")
          .withBoolean(on -> TokenSpacing.around(OPERATORS, on))
          .rule("mandatory-space-surrounding-operations")
          .whenOn(() -> TokenSpacing.around(OPERATORS, true))
          // Indentación: al final, para corregir todas las líneas que armaron las reglas de arriba
          .rule("indentSize", "indent-inside-if")
          .withInt(BlockIndentation::new)
          .build();

  private FormattingRules() {}

  /**
   * Arma reglas nuevas con las que la configuración deja activas, en el orden en que se aplican.
   *
   * @throws IllegalArgumentException si una clave tiene un valor que su regla no acepta
   */
  public static List<FormattingRule> create(RuleConfig config) {
    return REGISTRY.create(config);
  }

  /**
   * El estilo que se usa cuando no se configura ninguno: las reglas de la consigna, con un espacio
   * después del {@code :}, alrededor del {@code =} y de los operadores, una línea vacía antes de
   * cada {@code println} e indentación de 4 espacios.
   */
  public static RuleConfig defaults() {
    Map<String, Object> defaults = new LinkedHashMap<>();
    defaults.put("mandatory-line-break-after-statement", true);
    defaults.put("maxOneSpaceBetweenTokens", true);
    defaults.put("spaceBeforeColon", false);
    defaults.put("spaceAfterColon", true);
    defaults.put("spaceAroundAssign", true);
    defaults.put("spaceAroundOperators", true);
    defaults.put("newlineBeforePrintln", 1);
    defaults.put("indentSize", 4);
    return RuleConfig.of(defaults);
  }
}
