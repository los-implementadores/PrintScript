package org.printscript.formatter.rule;

import org.printscript.formatter.FormattingRule;
import org.printscript.formatter.Gap;
import org.printscript.token.TokenType;

/**
 * Deja {@code lines} líneas vacías antes de cada sentencia que llama a {@code println}, salvo que
 * sea la primera del archivo o de su bloque.
 */
public final class BlankLinesBeforePrintln implements FormattingRule {

  private final int lines;

  public BlankLinesBeforePrintln(int lines) {
    this.lines = lines;
  }

  @Override
  public String apply(Gap gap) {
    boolean afterAStatement = gap.isAfter(TokenType.SEMICOLON) || gap.isAfter(TokenType.RBRACE);
    if (afterAStatement && gap.isBeforeIdentifier("println")) {
      return gap.newLine(lines);
    }
    return gap.whitespace();
  }
}
