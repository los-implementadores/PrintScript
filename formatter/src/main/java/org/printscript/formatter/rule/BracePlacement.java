package org.printscript.formatter.rule;

import org.printscript.formatter.FormattingRule;
import org.printscript.formatter.Gap;
import org.printscript.token.TokenType;

/**
 * Dónde va la llave que abre un bloque ({@code if}, {@code else}): en la misma línea, separada por
 * un espacio, o sola en la línea de abajo.
 */
public final class BracePlacement implements FormattingRule {

  private final boolean sameLine;

  /**
   * @param sameLine {@code true} para dejar la llave en la línea del {@code if}, {@code false} para
   *     bajarla a la línea siguiente
   */
  public BracePlacement(boolean sameLine) {
    this.sameLine = sameLine;
  }

  @Override
  public String apply(Gap gap) {
    if (!gap.isBefore(TokenType.LBRACE) || gap.atStart()) {
      return gap.whitespace();
    }
    if (sameLine) {
      return " ";
    }
    return gap.breaksLine() ? gap.whitespace() : gap.newLine(0);
  }
}
