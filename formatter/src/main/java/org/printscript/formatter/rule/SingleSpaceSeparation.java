package org.printscript.formatter.rule;

import org.printscript.formatter.FormattingRule;
import org.printscript.formatter.Gap;
import org.printscript.token.TokenType;

/** Exactamente un espacio entre dos tokens de la misma línea, salvo antes del {@code ;}. */
public final class SingleSpaceSeparation implements FormattingRule {

  @Override
  public String apply(Gap gap) {
    if (!gap.insideLine()) {
      return gap.whitespace();
    }
    return gap.isBefore(TokenType.SEMICOLON) ? "" : " ";
  }
}
