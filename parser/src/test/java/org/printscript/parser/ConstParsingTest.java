package org.printscript.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.printscript.ast.LazyProgram;
import org.printscript.ast.Program;
import org.printscript.ast.Statement;
import org.printscript.ast.VarDeclarationStatement;
import org.printscript.lexer.Lexer;
import org.printscript.lexer.LexerFactory;

/** Tests de #32: declaración de constantes con {@code const}. */
class ConstParsingTest {

  private List<Statement> parse(String source, GrammarFeature... features) {
    Lexer lexer = LexerFactory.create(new StringReader(source));
    Parser parser = new ParserImpl(lexer, ParserGrammar.of(features));
    Program program = new LazyProgram(parser);
    List<Statement> statements = new ArrayList<>();
    program.stream().forEachRemaining(statements::add);
    return statements;
  }

  @Test
  void parsesConstDeclarationAsImmutable() {
    List<Statement> stmts = parse("const pi: number = 3;", GrammarFeature.values());
    VarDeclarationStatement decl = (VarDeclarationStatement) stmts.get(0);
    assertEquals("pi", decl.getName().getName());
    assertEquals("number", decl.getTypeName());
    assertTrue(decl.isConst());
  }

  @Test
  void letDeclarationIsNotConst() {
    List<Statement> stmts = parse("let x: number = 3;", GrammarFeature.values());
    VarDeclarationStatement decl = (VarDeclarationStatement) stmts.get(0);
    assertFalse(decl.isConst());
  }

  @Test
  void constRejectedWithoutItsFeature() {
    // Sin la feature, `const` no tiene statement parselet -> se parsea como expresión y falla.
    assertThrows(Exception.class, () -> parse("const pi: number = 3;", GrammarFeature.CORE));
  }
}
