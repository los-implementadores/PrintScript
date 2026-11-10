package org.printscript.formatter.rule;

import org.printscript.formatter.FormattingRule;
import org.printscript.formatter.Gap;
import org.printscript.token.TokenType;

/**
 * Indenta cada línea con {@code size} espacios por cada bloque {@code { }} que la contiene. Respeta
 * las líneas vacías que había, pero sin espacios adentro.
 *
 * <p>Va última entre las reglas: corrige el comienzo de todas las líneas, incluidas las que
 * cortaron las reglas anteriores.
 */
public final class BlockIndentation implements FormattingRule {

  private final int size;
  private int depth;

  public BlockIndentation(int size) {
    this.size = size;
  }

  @Override
  public String apply(Gap gap) {
    if (gap.isAfter(TokenType.LBRACE)) {
      depth++;
    }
    if (gap.isBefore(TokenType.RBRACE)) {
      depth = Math.max(0, depth - 1);
    }
    if (!gap.startsLine()) {
      return gap.whitespace();
    }
    return "\n".repeat(gap.lineBreaks()) + " ".repeat(depth * size);
  }
}
