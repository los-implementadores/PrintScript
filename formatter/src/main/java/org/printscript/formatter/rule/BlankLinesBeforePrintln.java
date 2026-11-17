package org.printscript.formatter.rule;

import org.printscript.formatter.FormattingRule;
import org.printscript.formatter.Gap;
import org.printscript.formatter.GapKind;

/**
 * Deja {@code lines} líneas vacías antes de cada sentencia {@code println}, salvo que sea la
 * primera del archivo o de su bloque.
 */
public final class BlankLinesBeforePrintln implements FormattingRule {

  private final int lines;

  public BlankLinesBeforePrintln(int lines) {
    this.lines = lines;
  }

  @Override
  public String apply(Gap gap) {
    if (gap.kind() == GapKind.BETWEEN_STATEMENTS && PrintlnStatement.is(gap.node())) {
      return gap.newLine(lines);
    }
    return gap.whitespace();
  }
}
