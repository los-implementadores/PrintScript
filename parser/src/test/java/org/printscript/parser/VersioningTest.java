package org.printscript.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.printscript.common.LanguageVersion;
import org.printscript.common.Position;
import org.printscript.common.ast.LazyProgram;
import org.printscript.common.ast.Program;
import org.printscript.common.ast.Statement;
import org.printscript.common.ast.VarDeclarationStatement;
import org.printscript.lexer.Lexer;
import org.printscript.lexer.LexerFactory;

/**
 * Tests de #30: versionado del lenguaje. Verifica que el parser expone gramáticas distintas por
 * versión y que una feature de 1.1 (tipo {@code boolean} en declaración) es rechazada en 1.0 y
 * aceptada en 1.1.
 */
class VersioningTest {

  private List<Statement> parseWith(String source, LanguageVersion version) {
    Lexer lexer = LexerFactory.create(new StringReader(source));
    Parser parser = new ParserImpl(lexer, version);
    Program program = new LazyProgram(parser, new Position(1, 1, 1, 1));
    List<Statement> statements = new ArrayList<>();
    program.stream().forEachRemaining(statements::add);
    return statements;
  }

  @Test
  void fromLabelResolvesSupportedVersions() {
    assertEquals(LanguageVersion.V1_0, LanguageVersion.fromLabel("1.0"));
    assertEquals(LanguageVersion.V1_1, LanguageVersion.fromLabel("1.1"));
  }

  @Test
  void fromLabelRejectsUnsupportedVersion() {
    assertThrows(IllegalArgumentException.class, () -> LanguageVersion.fromLabel("2.0"));
  }

  @Test
  void booleanTypeIsAllowedInVersion11() {
    List<Statement> stmts = parseWith("let ok: boolean = 1;", LanguageVersion.V1_1);
    VarDeclarationStatement decl = (VarDeclarationStatement) stmts.get(0);
    assertEquals("boolean", decl.getTypeName());
  }

  @Test
  void booleanTypeIsRejectedInVersion10() {
    ParseException ex =
        assertThrows(
            ParseException.class, () -> parseWith("let ok: boolean = 1;", LanguageVersion.V1_0));
    assertTrue(ex.getMessage().toLowerCase().contains("type"));
  }
}
