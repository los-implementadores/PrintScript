package org.printscript.formatter.rule;

import org.printscript.formatter.FormattingRule;
import org.printscript.formatter.Gap;
import org.printscript.formatter.GapKind;

/**
 * Deja {@code lines} líneas vacías después de cada sentencia {@code println}, si en la línea
 * siguiente sigue otra sentencia. No agrega líneas al final del archivo ni antes de la llave que
 * cierra un bloque, porque esas brechas no son entre sentencias.
 */
public final class BlankLinesAfterPrintln implements FormattingRule {

  private final int lines;

  public BlankLinesAfterPrintln(int lines) {
    this.lines = lines;
  }

  @Override
  public String apply(Gap gap) {
    if (gap.kind() == GapKind.BETWEEN_STATEMENTS
        && gap.breaksLine()
        && PrintlnStatement.is(gap.previous())) {
      return gap.newLine(lines);
    }
    return gap.whitespace();
  }
}
