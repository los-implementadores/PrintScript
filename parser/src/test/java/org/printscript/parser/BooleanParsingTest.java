package org.printscript.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.printscript.common.LanguageVersion;
import org.printscript.common.Position;
import org.printscript.common.ast.BooleanLiteral;
import org.printscript.common.ast.LazyProgram;
import org.printscript.common.ast.Program;
import org.printscript.common.ast.Statement;
import org.printscript.common.ast.VarDeclarationStatement;
import org.printscript.lexer.Lexer;
import org.printscript.lexer.LexerFactory;

/** Tests de #31: tipo y literales boolean en PrintScript 1.1. */
class BooleanParsingTest {

  private List<Statement> parse(String source, LanguageVersion version) {
    Lexer lexer = LexerFactory.create(new StringReader(source));
    Parser parser = new ParserImpl(lexer, version);
    Program program = new LazyProgram(parser, new Position(1, 1, 1, 1));
    List<Statement> statements = new ArrayList<>();
    program.stream().forEachRemaining(statements::add);
    return statements;
  }

  @Test
  void parsesBooleanDeclarationTrue() {
    List<Statement> stmts = parse("let ok: boolean = true;", LanguageVersion.V1_1);
    VarDeclarationStatement decl = (VarDeclarationStatement) stmts.get(0);
    assertEquals("boolean", decl.getTypeName());
    assertTrue(((BooleanLiteral) decl.getInitializer()).getValue());
  }

  @Test
  void parsesBooleanLiteralFalse() {
    List<Statement> stmts = parse("let ok: boolean = false;", LanguageVersion.V1_1);
    VarDeclarationStatement decl = (VarDeclarationStatement) stmts.get(0);
    assertFalse(((BooleanLiteral) decl.getInitializer()).getValue());
  }

  @Test
  void booleanLiteralRejectedInV1_0() {
    // El literal true no tiene parselet prefijo en 1.0 -> ParseException.
    assertThrows(
        ParseException.class, () -> parse("let ok: boolean = true;", LanguageVersion.V1_0));
  }
}
