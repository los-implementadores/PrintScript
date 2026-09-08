package org.printscript.parser;

import static org.junit.jupiter.api.Assertions.*;

import java.io.StringReader;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.printscript.common.LanguageVersion;
import org.printscript.common.Position;
import org.printscript.common.ast.IfStatement;
import org.printscript.common.ast.LazyProgram;
import org.printscript.common.ast.Program;
import org.printscript.common.ast.Statement;
import org.printscript.common.token.TokenType;
import org.printscript.lexer.*;

/** Tests de #33: condicionales if/else con bloques (PrintScript 1.1). */
class IfStatementParsingTest {

  private static final List<TokenMatcher> MATCHERS =
      List.of(
          new IdentifierTokenMatcher(
              Map.ofEntries(
                  Map.entry("let", TokenType.LET),
                  Map.entry("if", TokenType.IF),
                  Map.entry("else", TokenType.ELSE),
                  Map.entry("number", TokenType.TYPE_NUMBER),
                  Map.entry("boolean", TokenType.TYPE_BOOLEAN),
                  Map.entry("true", TokenType.BOOLEAN_LITERAL),
                  Map.entry("false", TokenType.BOOLEAN_LITERAL))),
          new NumberTokenMatcher(),
          new StringTokenMatcher(),
          new SymbolTokenMatcher(
              Map.ofEntries(
                  Map.entry(':', TokenType.COLON),
                  Map.entry('=', TokenType.ASSIGN),
                  Map.entry(';', TokenType.SEMICOLON),
                  Map.entry('(', TokenType.LPAREN),
                  Map.entry(')', TokenType.RPAREN),
                  Map.entry('{', TokenType.LBRACE),
                  Map.entry('}', TokenType.RBRACE))));

  private List<Statement> parse(String source, LanguageVersion version) {
    LexerImpl lexer = new LexerImpl(new StringReader(source), MATCHERS);
    Parser parser = new ParserImpl(lexer, version);
    Program program = new LazyProgram(parser, new Position(1, 1, 1, 1));
    return program.toList();
  }

  @Test
  void parsesSimpleIf() {
    List<Statement> stmts = parse("if (true) { let x: number = 1; }", LanguageVersion.V1_1);
    IfStatement ifStmt = (IfStatement) stmts.get(0);
    assertEquals(1, ifStmt.getThenBlock().size());
    assertFalse(ifStmt.hasElse());
  }

  @Test
  void parsesIfElse() {
    List<Statement> stmts =
        parse(
            "if (false) { let x: number = 1; } else { let y: number = 2; }", LanguageVersion.V1_1);
    IfStatement ifStmt = (IfStatement) stmts.get(0);
    assertEquals(1, ifStmt.getThenBlock().size());
    assertTrue(ifStmt.hasElse());
    assertEquals(1, ifStmt.getElseBlock().size());
  }

  @Test
  void rejectsElseIf() {
    ParseException ex =
        assertThrows(
            ParseException.class,
            () -> parse("if (true) { } else if (false) { }", LanguageVersion.V1_1));
    assertTrue(ex.getMessage().contains("else if"));
  }

  @Test
  void ifRejectedInV1_0() {
    assertThrows(Exception.class, () -> parse("if (true) { }", LanguageVersion.V1_0));
  }
}
