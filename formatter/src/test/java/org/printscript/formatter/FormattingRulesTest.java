package org.printscript.formatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import org.junit.jupiter.api.Test;

/** Fija cómo se leen las configuraciones, para que cambiar la librería de JSON no cambie nada. */
class FormattingRulesTest {

  private FormattingRules parse(String json) {
    return FormattingRules.fromJson(new StringReader(json));
  }

  @Test
  void readsTckKeys() {
    FormattingRules rules =
        parse(
            """
            {
              "enforce-spacing-before-colon-in-declaration": true,
              "enforce-spacing-after-colon-in-declaration": true,
              "enforce-spacing-around-equals": true,
              "enforce-no-spacing-around-equals": true,
              "mandatory-space-surrounding-operations": true,
              "line-breaks-after-println": 2,
              "indent-inside-if": 3,
              "mandatory-line-break-after-statement": true,
              "mandatory-single-space-separation": true,
              "if-brace-below-line": true,
              "if-brace-same-line": true
            }
            """);

    assertTrue(rules.isSpaceBeforeColon());
    assertTrue(rules.isSpaceAfterColon());
    assertTrue(rules.isSpaceAroundAssign());
    assertTrue(rules.isNoSpacingAroundAssign());
    assertTrue(rules.isSpaceAroundOperators());
    assertEquals(2, rules.getNewlineBeforePrintln());
    assertEquals(3, rules.getIndentSize());
    assertTrue(rules.isLineBreakAfterStatement());
    assertTrue(rules.isSingleSpaceSeparation());
    assertTrue(rules.isIfBraceBelowLine());
    assertTrue(rules.isIfBraceSameLine());
  }

  @Test
  void readsCamelCaseKeys() {
    FormattingRules rules =
        parse(
            """
            {"spaceBeforeColon": true, "spaceAfterColon": false, "spaceAroundAssign": false,
             "spaceAroundOperators": false, "newlineBeforePrintln": 0, "indentSize": 2}
            """);

    assertTrue(rules.isSpaceBeforeColonAst());
    assertFalse(rules.isSpaceAfterColonAst());
    assertFalse(rules.isSpaceAroundAssignAst());
    assertFalse(rules.isSpaceAroundOperatorsAst());
    assertEquals(0, rules.getNewlineBeforePrintln());
    assertEquals(2, rules.getIndentSize());
  }

  @Test
  void missingKeysKeepAstDefaultsAndLeaveStreamingRulesOff() {
    FormattingRules rules = parse("{}");

    assertTrue(rules.isSpaceAfterColonAst());
    assertTrue(rules.isSpaceAroundAssignAst());
    assertEquals(1, rules.getNewlineBeforePrintln());
    assertEquals(4, rules.getIndentSize());
    assertFalse(rules.isSpaceAfterColon());
    assertFalse(rules.hasIndentSize());
    assertFalse(rules.hasNewlineBeforePrintln());
  }

  @Test
  void ignoresUnknownKeys() {
    FormattingRules rules = parse("{\"some-future-rule\": true, \"indent-inside-if\": 8}");

    assertEquals(8, rules.getIndentSize());
  }

  @Test
  void rejectsMalformedJson() {
    assertThrows(IllegalArgumentException.class, () -> parse("{\"indent-inside-if\": "));
  }
}
