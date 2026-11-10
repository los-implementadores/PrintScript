package org.printscript.formatter.rule;

import org.printscript.formatter.FormattingRule;
import org.printscript.formatter.Gap;

/**
 * Como mucho un espacio entre dos tokens de la misma línea: varios espacios o tabs quedan en uno, y
 * donde no había ninguno sigue sin haber.
 */
public final class MaxOneSpace implements FormattingRule {

  @Override
  public String apply(Gap gap) {
    if (!gap.insideLine() || gap.whitespace().isEmpty()) {
      return gap.whitespace();
    }
    return " ";
  }
}
