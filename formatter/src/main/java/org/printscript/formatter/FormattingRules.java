package org.printscript.formatter;

import com.google.gson.Gson;
import java.io.Reader;

/**
 * Reglas de formateo configurables para el formatter de PrintScript.
 *
 * <p>Se carga desde un archivo JSON. Cada campo tiene un valor por defecto razonable para que el
 * formatter funcione sin configuración explícita.
 *
 * <p>Ejemplo de JSON:
 *
 * <pre>{@code
 * {
 *   "spaceBeforeColon": false,
 *   "spaceAfterColon": true,
 *   "spaceAroundAssign": true,
 *   "spaceAroundOperators": true,
 *   "newlineBeforePrintln": 1
 * }
 * }</pre>
 */
public class FormattingRules {

  private boolean spaceBeforeColon = false;
  private boolean spaceAfterColon = true;
  private boolean spaceAroundAssign = true;
  private boolean spaceAroundOperators = true;
  private int newlineBeforePrintln = 1;
  private int indentSize = 4;

  /** Constructor con valores por defecto. */
  public FormattingRules() {}

  /**
   * Carga las reglas desde un JSON.
   *
   * @param reader fuente de caracteres del JSON
   * @return las reglas parseadas
   */
  public static FormattingRules fromJson(Reader reader) {
    return new Gson().fromJson(reader, FormattingRules.class);
  }

  /** Si se agrega un espacio antes del {@code :} en declaraciones ({@code let x : number}). */
  public boolean isSpaceBeforeColon() {
    return spaceBeforeColon;
  }

  /** Si se agrega un espacio después del {@code :} en declaraciones ({@code let x: number}). */
  public boolean isSpaceAfterColon() {
    return spaceAfterColon;
  }

  /** Si se agrega un espacio alrededor del {@code =} ({@code let x: number = 5}). */
  public boolean isSpaceAroundAssign() {
    return spaceAroundAssign;
  }

  /** Si se agrega un espacio alrededor de operadores ({@code a + b}). */
  public boolean isSpaceAroundOperators() {
    return spaceAroundOperators;
  }

  /**
   * Cantidad de líneas en blanco a insertar antes de un {@code println} (u otra llamada a función).
   * Valor 0 = ninguna línea extra, 1 = una línea en blanco antes.
   */
  public int getNewlineBeforePrintln() {
    return newlineBeforePrintln;
  }

  /**
   * Cantidad de espacios de indentación para el contenido de un bloque (ej. dentro de un {@code
   * if}). Configurable desde el JSON; por defecto 4.
   */
  public int getIndentSize() {
    return indentSize;
  }
}
