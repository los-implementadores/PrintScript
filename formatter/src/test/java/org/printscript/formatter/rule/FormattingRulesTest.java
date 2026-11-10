package org.printscript.formatter.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.printscript.formatter.FormatterImpl;
import org.printscript.lexer.LexerFactory;
import org.printscript.rules.RuleConfig;

/** Cada regla del formatter, activada con su clave como en el archivo de configuración. */
class FormattingRulesTest {

  private static String format(RuleConfig config, String source) {
    StringWriter target = new StringWriter();
    new FormatterImpl(config)
        .format(LexerFactory.createWithWhitespace(new StringReader(source)), target);
    return target.toString();
  }

  private static String format(String json, String source) {
    return format(RuleConfig.fromJson(new StringReader(json)), source);
  }

  // --- Claves del TCK: con false la regla queda apagada ---

  @Test
  void spacesAroundTheAssign() {
    String input = "let x: number= 5;\nlet y: number =10;";

    assertEquals(
        "let x: number = 5;\nlet y: number = 10;",
        format("{\"enforce-spacing-around-equals\": true}", input));
    assertEquals(
        "let x: number=5;\nlet y: number=10;",
        format("{\"enforce-no-spacing-around-equals\": true}", input));
    assertEquals(input, format("{\"enforce-spacing-around-equals\": false}", input));
  }

  @Test
  void spacesAroundTheColon() {
    String input = "let a:string = \"x\";\nlet b : string = \"y\";";

    assertEquals(
        "let a: string = \"x\";\nlet b : string = \"y\";",
        format("{\"enforce-spacing-after-colon-in-declaration\": true}", input));
    assertEquals(
        "let a :string = \"x\";\nlet b : string = \"y\";",
        format("{\"enforce-spacing-before-colon-in-declaration\": true}", input));
  }

  @Test
  void spacesAroundTheOperators() {
    assertEquals(
        "let r: number = 5 + 4 * 3 / 2 - 1;",
        format("{\"mandatory-space-surrounding-operations\": true}", "let r: number = 5+4*3/2-1;"));
  }

  @Test
  void aLineBreakAfterEachStatement() {
    assertEquals(
        "let a: number = 1;\nlet b: number = 2;",
        format(
            "{\"mandatory-line-break-after-statement\": true}",
            "let a: number = 1;let b: number = 2;"));
  }

  @Test
  void exactlyOneSpaceBetweenTokens() {
    assertEquals(
        "let something : string = \"hello\";\nprintln ( something );",
        format(
            "{\"mandatory-single-space-separation\": true}",
            "let   something:   string=\"hello\";\nprintln(something);"));
  }

  @Test
  void theIfBraceOnTheSameLineOrBelow() {
    assertEquals(
        "if (true) {\n  println(\"ok\");\n}",
        format("{\"if-brace-same-line\": true}", "if (true)\n{\n  println(\"ok\");\n}"));
    assertEquals(
        "if (true)\n{\n  println(\"ok\");\n}",
        format("{\"if-brace-below-line\": true}", "if (true) {\n  println(\"ok\");\n}"));
  }

  @Test
  void indentationInsideNestedBlocks() {
    assertEquals(
        "if (a) {\n    if (b) {\n        println(\"nested\");\n    }\n} else {\n    x = 1;\n}",
        format(
            "{\"indent-inside-if\": 4}",
            "if (a) {\n  if (b) {\n    println(\"nested\");\n  }\n} else {\nx = 1;\n}"));
  }

  @Test
  void indentationKeepsBlankLinesButNotTheSpacesInThem() {
    assertEquals(
        "if (a) {\n  x;\n\n  y;\n}",
        format("{\"indentSize\": 2}", "if (a) {\n x;\n   \n      y;\n}"));
  }

  @Test
  void blankLinesAfterPrintln() {
    String input = "let x: number = 1;\nprintln(x);\n\n\nprintln(\"end\");";

    assertEquals(
        "let x: number = 1;\nprintln(x);\nprintln(\"end\");",
        format("{\"line-breaks-after-println\": 0}", input));
    assertEquals(
        "let x: number = 1;\nprintln(x);\n\nprintln(\"end\");",
        format("{\"line-breaks-after-println\": 1}", input));
    assertEquals(
        "let x: number = 1;\nprintln(x);\n\n\nprintln(\"end\");",
        format("{\"line-breaks-after-println\": 2}", input));
  }

  @Test
  void noBlankLinesAfterPrintlnBeforeAClosingBraceOrAtTheEnd() {
    String input = "if (a) {\n  println(x);\n}\nprintln(y);\n";

    assertEquals(input, format("{\"line-breaks-after-println\": 2}", input));
  }

  @Test
  void onlyTheConfiguredRulesTouchTheCode() {
    assertEquals(
        "let x: string = 5;\nlet y : string=10;",
        format(
            "{\"enforce-spacing-after-colon-in-declaration\": true}",
            "let x:string = 5;\nlet y : string=10;"));
  }

  @Test
  void stringsAreNeverTouched() {
    String input = "println(\"a=b : c+d;e\");";

    assertEquals(
        input,
        format(
            RuleConfig.of(
                Map.of(
                    "mandatory-line-break-after-statement", true,
                    "spaceAroundAssign", true,
                    "spaceBeforeColon", true,
                    "spaceAroundOperators", true)),
            input));
  }

  // --- Claves propias: con false se pide lo contrario ---

  @Test
  void ownColonKeys() {
    String input = "let x: number = 5;";

    assertEquals(
        "let x : number = 5;",
        format("{\"spaceBeforeColon\": true, \"spaceAfterColon\": true}", input));
    assertEquals(
        "let x:number = 5;",
        format("{\"spaceBeforeColon\": false, \"spaceAfterColon\": false}", input));
  }

  @Test
  void ownAssignAndOperatorKeys() {
    assertEquals(
        "let x: number=5;", format("{\"spaceAroundAssign\": false}", "let x: number = 5;"));
    assertEquals(
        "let r: number = 2+3;",
        format("{\"spaceAroundOperators\": false}", "let r: number = 2 + 3;"));
  }

  @Test
  void blankLinesBeforePrintln() {
    String input = "println(1);\nlet x: number = 1;\nprintln(x);";

    assertEquals(input, format("{\"newlineBeforePrintln\": 0}", input));
    assertEquals(
        "println(1);\nlet x: number = 1;\n\n\nprintln(x);",
        format("{\"newlineBeforePrintln\": 2}", input));
  }

  @Test
  void atMostOneSpaceBetweenTokens() {
    assertEquals(
        "let x: number = a+b;",
        format("{\"maxOneSpaceBetweenTokens\": true}", "let   x:\t number  =   a+b;"));
  }

  // --- Estilo por defecto ---

  @Test
  void theDefaultStyleFormatsAWholeProgram() {
    String input =
        "let name:string=\"Joe\";\nlet lastName:string =\"Doe\";\nprintln(name +\" \"+ lastName);";

    assertEquals(
        "let name: string = \"Joe\";\nlet lastName: string = \"Doe\";\n\n"
            + "println(name + \" \" + lastName);",
        format(FormattingRules.defaults(), input));
  }

  @Test
  void theDefaultStyleIndentsBlocksAndSplitsStatements() {
    assertEquals(
        "let a: boolean = true;\nif (a) {\n    println(\"a\");\n} else {\n    println(\"b\");\n}",
        format(
            FormattingRules.defaults(),
            "let a:boolean=true; if (a) {\nprintln(\"a\");\n} else {\n  println(\"b\");\n}"));
  }
}
