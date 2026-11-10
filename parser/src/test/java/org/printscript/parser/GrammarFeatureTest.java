package org.printscript.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.printscript.ast.Statement;
import org.printscript.ast.VarDeclarationStatement;
import org.printscript.lexer.LexerFactory;
import org.printscript.parser.parselet.statement.VarDeclarationParselet;
import org.printscript.token.TokenType;

/** El parser entiende exactamente lo que trae su gramática, sin saber de versiones del lenguaje. */
class GrammarFeatureTest {

  private List<Statement> parse(String source, ParserGrammar grammar) {
    List<Statement> statements = new ArrayList<>();
    new ParserImpl(LexerFactory.create(new StringReader(source)), grammar)
        .forEachRemaining(statements::add);
    return statements;
  }

  @Test
  void aTypeIsOnlyAcceptedIfTheGrammarRegistersIt() {
    String source = "let ok: boolean = 1;";

    VarDeclarationStatement declaration =
        (VarDeclarationStatement)
            parse(source, ParserGrammar.of(GrammarFeature.CORE, GrammarFeature.BOOLEANS)).get(0);
    assertEquals("boolean", declaration.getTypeName());

    ParseException e =
        assertThrows(
            ParseException.class, () -> parse(source, ParserGrammar.of(GrammarFeature.CORE)));
    assertTrue(e.getMessage().contains("type"));
  }

  @Test
  void aGrammarCanBeExtendedWithoutTouchingTheParser() {
    ParserGrammar.Builder builder = ParserGrammar.builder();
    GrammarFeature.CORE.addTo(builder);
    // `const` reusa el parselet de `let`: registrarlo es lo único que hace falta.
    ParserGrammar grammar =
        builder
            .statement(TokenType.CONST, new VarDeclarationParselet(TokenType.CONST, true))
            .build();

    VarDeclarationStatement declaration =
        (VarDeclarationStatement) parse("const x: number = 1;", grammar).get(0);

    assertTrue(declaration.isConst());
  }
}
