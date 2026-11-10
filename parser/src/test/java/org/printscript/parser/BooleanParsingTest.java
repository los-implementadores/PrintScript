package org.printscript.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.printscript.ast.BooleanLiteral;
import org.printscript.ast.LazyProgram;
import org.printscript.ast.Program;
import org.printscript.ast.Statement;
import org.printscript.ast.VarDeclarationStatement;
import org.printscript.lexer.Lexer;
import org.printscript.lexer.LexerFactory;

/** Tests de #31: tipo y literales boolean ({@link GrammarFeature#BOOLEANS}). */
class BooleanParsingTest {

  private List<Statement> parse(String source, GrammarFeature... features) {
    Lexer lexer = LexerFactory.create(new StringReader(source));
    Parser parser = new ParserImpl(lexer, ParserGrammar.of(features));
    Program program = new LazyProgram(parser);
    List<Statement> statements = new ArrayList<>();
    program.stream().forEachRemaining(statements::add);
    return statements;
  }

  @Test
  void parsesBooleanDeclarationTrue() {
    List<Statement> stmts = parse("let ok: boolean = true;", GrammarFeature.values());
    VarDeclarationStatement decl = (VarDeclarationStatement) stmts.get(0);
    assertEquals("boolean", decl.getTypeName());
    assertTrue(((BooleanLiteral) decl.getInitializer()).getValue());
  }

  @Test
  void parsesBooleanLiteralFalse() {
    List<Statement> stmts = parse("let ok: boolean = false;", GrammarFeature.values());
    VarDeclarationStatement decl = (VarDeclarationStatement) stmts.get(0);
    assertFalse(((BooleanLiteral) decl.getInitializer()).getValue());
  }

  @Test
  void booleanLiteralRejectedWithoutItsFeature() {
    // Sin la feature, el literal true no tiene parselet prefijo -> ParseException.
    assertThrows(ParseException.class, () -> parse("let ok: boolean = true;", GrammarFeature.CORE));
  }
}
