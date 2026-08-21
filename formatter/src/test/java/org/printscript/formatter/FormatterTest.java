package org.printscript.formatter;

import static org.junit.jupiter.api.Assertions.*;

import java.io.StringReader;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.printscript.common.Position;
import org.printscript.common.ast.*;
import org.printscript.common.token.TokenType;
import org.printscript.lexer.*;
import org.printscript.parser.ParserImpl;

class FormatterTest {

  private static final Position POS = new Position(1, 1, 1, 1);

  private static final List<TokenMatcher> MATCHERS =
      List.of(
          new IdentifierTokenMatcher(
              Map.of(
                  "let", TokenType.LET,
                  "number", TokenType.TYPE_NUMBER,
                  "string", TokenType.TYPE_STRING)),
          new NumberTokenMatcher(),
          new StringTokenMatcher(),
          new SymbolTokenMatcher(
              Map.of(
                  ':', TokenType.COLON,
                  '=', TokenType.ASSIGN,
                  ';', TokenType.SEMICOLON,
                  '(', TokenType.LPAREN,
                  ')', TokenType.RPAREN,
                  '+', TokenType.PLUS,
                  '-', TokenType.MINUS,
                  '*', TokenType.STAR,
                  '/', TokenType.SLASH)));

  private Program parse(String source) {
    LexerImpl lexer = new LexerImpl(new StringReader(source), MATCHERS);
    ParserImpl parser = new ParserImpl(lexer);
    return new LazyProgram(parser, POS);
  }

  private Formatter defaultFormatter() {
    return new FormatterImpl(new FormattingRules());
  }

  // ---------------------------------------------------------------- Reglas por defecto

  @Test
  void formatsVarDeclarationWithDefaultRules() {
    Program program = parse("let x: number = 5;");
    String result = defaultFormatter().format(program);
    assertEquals("let x: number = 5;\n", result);
  }

  @Test
  void formatsStringDeclaration() {
    Program program = parse("let name: string = \"Joe\";");
    String result = defaultFormatter().format(program);
    assertEquals("let name: string = \"Joe\";\n", result);
  }

  @Test
  void formatsAssignment() {
    Program program = parse("let x: number = 1;\nx = 99;");
    String result = defaultFormatter().format(program);
    assertEquals("let x: number = 1;\nx = 99;\n", result);
  }

  @Test
  void formatsPrintln() {
    Program program = parse("let x: number = 1;\nprintln(x);");
    String result = defaultFormatter().format(program);
    // Con newlineBeforePrintln=1, hay una linea en blanco antes del println
    assertEquals("let x: number = 1;\n\nprintln(x);\n", result);
  }

  @Test
  void formatsBinaryExpression() {
    Program program = parse("let r: number = 2 + 3 * 4;");
    String result = defaultFormatter().format(program);
    assertEquals("let r: number = 2 + 3 * 4;\n", result);
  }

  // ---------------------------------------------------------------- Código desformateado

  @Test
  void fixesMissingSpacesAroundAssign() {
    // Input sin espacios alrededor de =
    Program program = parse("let x: number =5;");
    String result = defaultFormatter().format(program);
    assertEquals("let x: number = 5;\n", result);
  }

  @Test
  void fixesMissingSpacesAroundOperators() {
    Program program = parse("let r: number = 2+3;");
    String result = defaultFormatter().format(program);
    assertEquals("let r: number = 2 + 3;\n", result);
  }

  // ---------------------------------------------------------------- Configuración custom

  @Test
  void respectsSpaceBeforeColon() {
    FormattingRules rules =
        FormattingRules.fromJson(
            new StringReader("{\"spaceBeforeColon\": true, \"spaceAfterColon\": true}"));
    Formatter formatter = new FormatterImpl(rules);

    Program program = parse("let x: number = 5;");
    String result = formatter.format(program);
    assertEquals("let x : number = 5;\n", result);
  }

  @Test
  void respectsNoSpaceAfterColon() {
    FormattingRules rules =
        FormattingRules.fromJson(
            new StringReader("{\"spaceBeforeColon\": false, \"spaceAfterColon\": false}"));
    Formatter formatter = new FormatterImpl(rules);

    Program program = parse("let x: number = 5;");
    String result = formatter.format(program);
    assertEquals("let x:number = 5;\n", result);
  }

  @Test
  void respectsNoSpaceAroundAssign() {
    FormattingRules rules =
        FormattingRules.fromJson(new StringReader("{\"spaceAroundAssign\": false}"));
    Formatter formatter = new FormatterImpl(rules);

    Program program = parse("let x: number = 5;");
    String result = formatter.format(program);
    assertEquals("let x: number=5;\n", result);
  }

  @Test
  void respectsNoSpaceAroundOperators() {
    FormattingRules rules =
        FormattingRules.fromJson(new StringReader("{\"spaceAroundOperators\": false}"));
    Formatter formatter = new FormatterImpl(rules);

    Program program = parse("let r: number = 2 + 3;");
    String result = formatter.format(program);
    assertEquals("let r: number = 2+3;\n", result);
  }

  @Test
  void respectsNewlineBeforePrintlnZero() {
    FormattingRules rules =
        FormattingRules.fromJson(new StringReader("{\"newlineBeforePrintln\": 0}"));
    Formatter formatter = new FormatterImpl(rules);

    Program program = parse("let x: number = 1;\nprintln(x);");
    String result = formatter.format(program);
    assertEquals("let x: number = 1;\nprintln(x);\n", result);
  }

  // ---------------------------------------------------------------- Programa completo

  @Test
  void formatsFullProgram() {
    String source =
        "let name: string = \"Joe\";\n"
            + "let lastName: string = \"Doe\";\n"
            + "println(name + \" \" + lastName);";

    Program program = parse(source);
    String result = defaultFormatter().format(program);

    String expected =
        "let name: string = \"Joe\";\n"
            + "let lastName: string = \"Doe\";\n"
            + "\n"
            + "println(name + \" \" + lastName);\n";

    assertEquals(expected, result);
  }

  @Test
  void formatsNumberProgramWithDivision() {
    String source =
        "let a: number = 12;\n"
            + "let b: number = 4;\n"
            + "let c: number = a / b;\n"
            + "println(c);";

    Program program = parse(source);
    String result = defaultFormatter().format(program);

    String expected =
        "let a: number = 12;\n"
            + "let b: number = 4;\n"
            + "let c: number = a / b;\n"
            + "\n"
            + "println(c);\n";

    assertEquals(expected, result);
  }

  // ---------------------------------------------------------------- FormattingRules desde JSON

  @Test
  void loadsRulesFromJson() {
    String json =
        "{\"spaceBeforeColon\": true, \"spaceAfterColon\": false,"
            + "\"spaceAroundAssign\": true, \"spaceAroundOperators\": false,"
            + "\"newlineBeforePrintln\": 2}";
    FormattingRules rules = FormattingRules.fromJson(new StringReader(json));

    assertTrue(rules.isSpaceBeforeColon());
    assertFalse(rules.isSpaceAfterColon());
    assertTrue(rules.isSpaceAroundAssign());
    assertFalse(rules.isSpaceAroundOperators());
    assertEquals(2, rules.getNewlineBeforePrintln());
  }
}
