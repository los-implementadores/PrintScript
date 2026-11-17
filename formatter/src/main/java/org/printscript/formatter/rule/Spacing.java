package org.printscript.formatter.rule;

import java.util.EnumSet;
import java.util.Set;
import org.printscript.formatter.FormattingRule;
import org.printscript.formatter.Gap;
import org.printscript.formatter.GapKind;

/**
 * Fija el espacio de ciertas brechas dentro de una línea: un espacio o ninguno. Sirve para el
 * {@code :} de las declaraciones, el {@code =} y los operadores. No toca los espacios que tienen un
 * salto de línea.
 */
public final class Spacing implements FormattingRule {

  private final Set<GapKind> kinds;
  private final String space;

  private Spacing(Set<GapKind> kinds, boolean withSpace) {
    this.kinds = Set.copyOf(kinds);
    this.space = withSpace ? " " : "";
  }

  /** Un espacio (o ninguno) en las brechas de tipo {@code first} y {@code rest}. */
  public static Spacing at(boolean withSpace, GapKind first, GapKind... rest) {
    return new Spacing(EnumSet.of(first, rest), withSpace);
  }

  @Override
  public String apply(Gap gap) {
    return gap.insideLine() && kinds.contains(gap.kind()) ? space : gap.whitespace();
  }
}
