package org.printscript.formatter.rule;

import org.printscript.formatter.FormattingRule;
import org.printscript.formatter.Gap;

/**
 * Indenta cada línea con {@code size} espacios por cada bloque que la contiene. Respeta las líneas
 * vacías que había, pero sin espacios adentro.
 *
 * <p>La profundidad la da el AST ({@code gap.shape().depth()}): la regla no cuenta llaves. Va
 * última entre las reglas porque corrige el comienzo de todas las líneas, incluidas las que
 * cortaron las reglas anteriores.
 */
public final class BlockIndentation implements FormattingRule {

  private final int size;

  public BlockIndentation(int size) {
    this.size = size;
  }

  @Override
  public String apply(Gap gap) {
    if (!gap.startsLine()) {
      return gap.whitespace();
    }
    return "\n".repeat(gap.lineBreaks()) + " ".repeat(gap.shape().depth() * size);
  }
}
