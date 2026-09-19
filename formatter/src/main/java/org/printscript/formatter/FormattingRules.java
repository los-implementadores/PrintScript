package org.printscript.formatter;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.io.Reader;

/**
 * Reglas de formateo configurables para el formatter de PrintScript.
 *
 * <p>Soporta configuraciones estándar en camelCase y las reglas específicas del TCK en kebab-case.
 */
public class FormattingRules {

  @SerializedName(
      value = "spaceBeforeColon",
      alternate = {"enforce-spacing-before-colon-in-declaration"})
  private Boolean spaceBeforeColon;

  @SerializedName(
      value = "spaceAfterColon",
      alternate = {"enforce-spacing-after-colon-in-declaration"})
  private Boolean spaceAfterColon;

  @SerializedName(
      value = "spaceAroundAssign",
      alternate = {"enforce-spacing-around-equals"})
  private Boolean spaceAroundAssign;

  @SerializedName("enforce-no-spacing-around-equals")
  private Boolean noSpacingAroundAssign;

  @SerializedName(
      value = "spaceAroundOperators",
      alternate = {"mandatory-space-surrounding-operations"})
  private Boolean spaceAroundOperators;

  @SerializedName(
      value = "newlineBeforePrintln",
      alternate = {"line-breaks-after-println"})
  private Integer newlineBeforePrintln;

  @SerializedName(
      value = "indentSize",
      alternate = {"indent-inside-if"})
  private Integer indentSize;

  @SerializedName("mandatory-line-break-after-statement")
  private Boolean lineBreakAfterStatement;

  @SerializedName("mandatory-single-space-separation")
  private Boolean singleSpaceSeparation;

  @SerializedName("if-brace-below-line")
  private Boolean ifBraceBelowLine;

  @SerializedName("if-brace-same-line")
  private Boolean ifBraceSameLine;

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

  // --- Getters para estrategia AST (con defaults históricos) ---

  public boolean isSpaceBeforeColonAst() {
    return spaceBeforeColon != null && spaceBeforeColon;
  }

  public boolean isSpaceAfterColonAst() {
    return spaceAfterColon == null || spaceAfterColon;
  }

  public boolean isSpaceAroundAssignAst() {
    return (spaceAroundAssign == null || spaceAroundAssign)
        && (noSpacingAroundAssign == null || !noSpacingAroundAssign);
  }

  public boolean isSpaceAroundOperatorsAst() {
    return spaceAroundOperators == null || spaceAroundOperators;
  }

  public int getNewlineBeforePrintln() {
    return newlineBeforePrintln != null ? newlineBeforePrintln : 1;
  }

  public int getIndentSize() {
    return indentSize != null ? indentSize : 4;
  }

  // --- Getters booleanos para la estrategia Streaming (sin defaults arbitrarios) ---

  public boolean isSpaceBeforeColon() {
    return spaceBeforeColon != null && spaceBeforeColon;
  }

  public boolean isSpaceAfterColon() {
    return spaceAfterColon != null && spaceAfterColon;
  }

  public boolean isSpaceAroundAssign() {
    return spaceAroundAssign != null && spaceAroundAssign;
  }

  public boolean isNoSpacingAroundAssign() {
    return noSpacingAroundAssign != null && noSpacingAroundAssign;
  }

  public boolean isSpaceAroundOperators() {
    return spaceAroundOperators != null && spaceAroundOperators;
  }

  public boolean isLineBreakAfterStatement() {
    return lineBreakAfterStatement != null && lineBreakAfterStatement;
  }

  public boolean isSingleSpaceSeparation() {
    return singleSpaceSeparation != null && singleSpaceSeparation;
  }

  public boolean isIfBraceBelowLine() {
    return ifBraceBelowLine != null && ifBraceBelowLine;
  }

  public boolean isIfBraceSameLine() {
    return ifBraceSameLine != null && ifBraceSameLine;
  }

  public boolean hasIndentSize() {
    return indentSize != null;
  }

  public boolean hasNewlineBeforePrintln() {
    return newlineBeforePrintln != null;
  }
}
