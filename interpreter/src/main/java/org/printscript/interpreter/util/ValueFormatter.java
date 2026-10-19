package org.printscript.interpreter.util;

/** Representación textual de un valor de PrintScript, tal como la imprime {@code println}. */
public final class ValueFormatter {

  private ValueFormatter() {}

  /** Los {@code number} enteros se muestran sin decimales: {@code 42.0} se imprime como "42". */
  public static String display(Object value) {
    if (value instanceof Double d && d % 1 == 0) {
      return String.valueOf(d.longValue());
    }
    return String.valueOf(value);
  }
}
