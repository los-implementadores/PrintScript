package org.printscript.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.printscript.ast.IfStatement;
import org.printscript.ast.LazyProgram;
import org.printscript.ast.Program;
import org.printscript.ast.Statement;
import org.printscript.lexer.Lexer;
import org.printscript.lexer.LexerFactory;
import org.printscript.source.Position;

/** Tests de #33: condicionales if/else con bloques. */
class IfStatementParsingTest {

  private List<Statement> parse(String source, GrammarFeature... features) {
    Lexer lexer = LexerFactory.create(new StringReader(source));
    Parser parser = new ParserImpl(lexer, ParserGrammar.of(features));
    Program program = new LazyProgram(parser, new Position(1, 1, 1, 1));
    List<Statement> statements = new ArrayList<>();
    program.stream().forEachRemaining(statements::add);
    return statements;
  }

  @Test
  void parsesSimpleIf() {
    List<Statement> stmts = parse("if (true) { let x: number = 1; }", GrammarFeature.values());
    IfStatement ifStmt = (IfStatement) stmts.get(0);
    assertEquals(1, ifStmt.getThenBlock().size());
    assertFalse(ifStmt.hasElse());
  }

  @Test
  void parsesIfElse() {
    List<Statement> stmts =
        parse(
            "if (false) { let x: number = 1; } else { let y: number = 2; }",
            GrammarFeature.values());
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
            () -> parse("if (true) { } else if (false) { }", GrammarFeature.values()));
    assertTrue(ex.getMessage().contains("else if"));
  }

  @Test
  void ifRejectedWithoutItsFeature() {
    assertThrows(Exception.class, () -> parse("if (true) { }", GrammarFeature.CORE));
  }
}
