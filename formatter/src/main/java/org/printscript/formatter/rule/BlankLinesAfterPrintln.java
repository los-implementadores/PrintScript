package org.printscript.formatter.rule;

import org.printscript.formatter.FormattingRule;
import org.printscript.formatter.Gap;
import org.printscript.token.TokenType;

/**
 * Deja {@code lines} líneas vacías después de cada sentencia que llama a {@code println}, si en la
 * línea siguiente sigue otra sentencia. No agrega líneas al final del archivo ni antes de la llave
 * que cierra un bloque.
 */
public final class BlankLinesAfterPrintln implements FormattingRule {

  private final int lines;
  private boolean inPrintln;

  public BlankLinesAfterPrintln(int lines) {
    this.lines = lines;
  }

  @Override
  public String apply(Gap gap) {
    String result = gap.whitespace();
    boolean endsPrintln = inPrintln && gap.isAfter(TokenType.SEMICOLON);
    if (endsPrintln && gap.breaksLine() && !gap.atEnd() && !gap.isBefore(TokenType.RBRACE)) {
      result = gap.newLine(lines);
    }
    if (startsStatement(gap)) {
      inPrintln = gap.isBeforeIdentifier("println");
    }
    return result;
  }

  private static boolean startsStatement(Gap gap) {
    return gap.atStart()
        || gap.isAfter(TokenType.SEMICOLON)
        || gap.isAfter(TokenType.LBRACE)
        || gap.isAfter(TokenType.RBRACE);
  }
}
