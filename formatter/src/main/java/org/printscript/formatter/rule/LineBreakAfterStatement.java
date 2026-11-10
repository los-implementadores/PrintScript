package org.printscript.formatter.rule;

import org.printscript.formatter.FormattingRule;
import org.printscript.formatter.Gap;
import org.printscript.token.TokenType;

/** Después de cada {@code ;} empieza una línea nueva, si en esa línea había algo más. */
public final class LineBreakAfterStatement implements FormattingRule {

  @Override
  public String apply(Gap gap) {
    if (gap.isAfter(TokenType.SEMICOLON) && gap.insideLine()) {
      return gap.newLine(0);
    }
    return gap.whitespace();
  }
}
