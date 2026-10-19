package org.printscript.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.printscript.common.LanguageVersion;
import org.printscript.common.Position;
import org.printscript.common.ast.IfStatement;
import org.printscript.common.ast.LazyProgram;
import org.printscript.common.ast.Program;
import org.printscript.common.ast.Statement;
import org.printscript.lexer.Lexer;
import org.printscript.lexer.LexerFactory;

/** Tests de #33: condicionales if/else con bloques (PrintScript 1.1). */
class IfStatementParsingTest {

  private List<Statement> parse(String source, LanguageVersion version) {
    Lexer lexer = LexerFactory.create(new StringReader(source));
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
