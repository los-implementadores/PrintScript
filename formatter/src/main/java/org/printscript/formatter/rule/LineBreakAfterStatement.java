package org.printscript.formatter.rule;

import org.printscript.formatter.FormattingRule;
import org.printscript.formatter.Gap;
import org.printscript.formatter.GapKind;

/**
 * Después de cada sentencia empieza una línea nueva, si en esa línea había algo más: otra sentencia
 * o la llave que cierra el bloque.
 */
public final class LineBreakAfterStatement implements FormattingRule {

  @Override
  public String apply(Gap gap) {
    boolean afterAStatement =
        gap.kind() == GapKind.BETWEEN_STATEMENTS
            || gap.kind() == GapKind.BLOCK_END && gap.previous() != null;
    if (afterAStatement && gap.insideLine()) {
      return gap.newLine(0);
    }
    return gap.whitespace();
  }
}
