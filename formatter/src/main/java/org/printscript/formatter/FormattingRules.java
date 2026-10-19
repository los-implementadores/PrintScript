package org.printscript.formatter;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.Reader;

/**
 * Reglas de formateo configurables para el formatter de PrintScript.
 *
 * <p>Soporta configuraciones estándar en camelCase y las reglas específicas del TCK en kebab-case.
 * Las claves desconocidas se ignoran.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FormattingRules {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @JsonProperty("spaceBeforeColon")
  @JsonAlias("enforce-spacing-before-colon-in-declaration")
  private Boolean spaceBeforeColon;

  @JsonProperty("spaceAfterColon")
  @JsonAlias("enforce-spacing-after-colon-in-declaration")
  private Boolean spaceAfterColon;

  @JsonProperty("spaceAroundAssign")
  @JsonAlias("enforce-spacing-around-equals")
  private Boolean spaceAroundAssign;

  @JsonProperty("enforce-no-spacing-around-equals")
  private Boolean noSpacingAroundAssign;

  @JsonProperty("spaceAroundOperators")
  @JsonAlias("mandatory-space-surrounding-operations")
  private Boolean spaceAroundOperators;

  @JsonProperty("newlineBeforePrintln")
  @JsonAlias("line-breaks-after-println")
  private Integer newlineBeforePrintln;

  @JsonProperty("indentSize")
  @JsonAlias("indent-inside-if")
  private Integer indentSize;

  @JsonProperty("mandatory-line-break-after-statement")
  private Boolean lineBreakAfterStatement;

  @JsonProperty("mandatory-single-space-separation")
  private Boolean singleSpaceSeparation;

  @JsonProperty("if-brace-below-line")
  private Boolean ifBraceBelowLine;

  @JsonProperty("if-brace-same-line")
  private Boolean ifBraceSameLine;

  /** Constructor con valores por defecto. */
  public FormattingRules() {}

  /**
   * Carga las reglas desde un JSON.
   *
   * @param reader fuente de caracteres del JSON
   * @return las reglas parseadas
   * @throws IllegalArgumentException si el JSON no es una configuración válida
   */
  public static FormattingRules fromJson(Reader reader) {
    try {
      return MAPPER.readValue(reader, FormattingRules.class);
    } catch (IOException e) {
      throw new IllegalArgumentException("Configuración de formato inválida: " + e.getMessage(), e);
    }
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
