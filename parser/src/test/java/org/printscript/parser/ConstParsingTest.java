package org.printscript.parser;

import static org.junit.jupiter.api.Assertions.*;

import java.io.StringReader;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.printscript.common.LanguageVersion;
import org.printscript.common.Position;
import org.printscript.common.ast.LazyProgram;
import org.printscript.common.ast.Program;
import org.printscript.common.ast.Statement;
import org.printscript.common.ast.VarDeclarationStatement;
import org.printscript.common.token.TokenType;
import org.printscript.lexer.*;

/** Tests de #32: declaración de constantes con {@code const} (PrintScript 1.1). */
class ConstParsingTest {

  private static final List<TokenMatcher> MATCHERS =
      List.of(
          new IdentifierTokenMatcher(
              Map.of(
                  "let", TokenType.LET,
                  "const", TokenType.CONST,
                  "number", TokenType.TYPE_NUMBER,
                  "string", TokenType.TYPE_STRING)),
          new NumberTokenMatcher(),
          new StringTokenMatcher(),
          new SymbolTokenMatcher(
              Map.of(
                  ':', TokenType.COLON,
                  '=', TokenType.ASSIGN,
                  ';', TokenType.SEMICOLON)));

  private List<Statement> parse(String source, LanguageVersion version) {
    LexerImpl lexer = new LexerImpl(new StringReader(source), MATCHERS);
    Parser parser = new ParserImpl(lexer, version);
    Program program = new LazyProgram(parser, new Position(1, 1, 1, 1));
    return program.toList();
  }

  @Test
  void parsesConstDeclarationAsImmutable() {
    List<Statement> stmts = parse("const pi: number = 3;", LanguageVersion.V1_1);
    VarDeclarationStatement decl = (VarDeclarationStatement) stmts.get(0);
    assertEquals("pi", decl.getName().getName());
    assertEquals("number", decl.getTypeName());
    assertTrue(decl.isConst());
  }

  @Test
  void letDeclarationIsNotConst() {
    List<Statement> stmts = parse("let x: number = 3;", LanguageVersion.V1_1);
    VarDeclarationStatement decl = (VarDeclarationStatement) stmts.get(0);
    assertFalse(decl.isConst());
  }

  @Test
  void constRejectedInV1_0() {
    // `const` no tiene statement parselet en 1.0 -> se parsea como default (expresión) y falla.
    assertThrows(Exception.class, () -> parse("const pi: number = 3;", LanguageVersion.V1_0));
  }
}
