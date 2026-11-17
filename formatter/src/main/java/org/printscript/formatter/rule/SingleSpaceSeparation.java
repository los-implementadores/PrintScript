package org.printscript.formatter.rule;

import org.printscript.formatter.FormattingRule;
import org.printscript.formatter.Gap;
import org.printscript.formatter.GapKind;

/** Exactamente un espacio en cada brecha de una misma línea, salvo antes del {@code ;}. */
public final class SingleSpaceSeparation implements FormattingRule {

  @Override
  public String apply(Gap gap) {
    if (!gap.insideLine()) {
      return gap.whitespace();
    }
    return gap.kind() == GapKind.BEFORE_SEMICOLON ? "" : " ";
  }
}
