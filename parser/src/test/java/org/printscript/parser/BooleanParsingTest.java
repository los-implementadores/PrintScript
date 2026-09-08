package org.printscript.parser;

import static org.junit.jupiter.api.Assertions.*;

import java.io.StringReader;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.printscript.common.LanguageVersion;
import org.printscript.common.Position;
import org.printscript.common.ast.BooleanLiteral;
import org.printscript.common.ast.LazyProgram;
import org.printscript.common.ast.Program;
import org.printscript.common.ast.Statement;
import org.printscript.common.ast.VarDeclarationStatement;
import org.printscript.common.token.TokenType;
import org.printscript.lexer.*;

/** Tests de #31: tipo y literales boolean en PrintScript 1.1. */
class BooleanParsingTest {

  private static final List<TokenMatcher> MATCHERS =
      List.of(
          new IdentifierTokenMatcher(
              Map.of(
                  "let", TokenType.LET,
                  "number", TokenType.TYPE_NUMBER,
                  "string", TokenType.TYPE_STRING,
                  "boolean", TokenType.TYPE_BOOLEAN,
                  "true", TokenType.BOOLEAN_LITERAL,
                  "false", TokenType.BOOLEAN_LITERAL)),
          new NumberTokenMatcher(),
          new StringTokenMatcher(),
          new SymbolTokenMatcher(
              Map.of(
                  ':', TokenType.COLON,
                  '=', TokenType.ASSIGN,
                  ';', TokenType.SEMICOLON,
                  '(', TokenType.LPAREN,
                  ')', TokenType.RPAREN)));

  private List<Statement> parse(String source, LanguageVersion version) {
    LexerImpl lexer = new LexerImpl(new StringReader(source), MATCHERS);
    Parser parser = new ParserImpl(lexer, version);
    Program program = new LazyProgram(parser, new Position(1, 1, 1, 1));
    return program.toList();
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
