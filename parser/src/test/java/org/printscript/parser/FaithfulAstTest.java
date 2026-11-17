package org.printscript.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.StringReader;
import org.junit.jupiter.api.Test;
import org.printscript.ast.BinaryExpression;
import org.printscript.ast.CallExpression;
import org.printscript.ast.Expression;
import org.printscript.ast.ExpressionStatement;
import org.printscript.ast.GroupExpression;
import org.printscript.ast.Identifier;
import org.printscript.ast.NumberLiteral;
import org.printscript.ast.StringLiteral;
import org.printscript.lexer.LexerFactory;

/**
 * El AST guarda lo que el formatter necesita para escribir el programa como estaba: los paréntesis
 * y el texto de los literales (ADR-029).
 */
class FaithfulAstTest {

  private static Expression printlnArgument(String source) {
    Parser parser =
        new ParserImpl(
            LexerFactory.create(new StringReader(source)),
            ParserGrammar.of(GrammarFeature.values()));
    ExpressionStatement statement = (ExpressionStatement) parser.next();
    return ((CallExpression) statement.getExpression()).getArguments().get(0);
  }

  @Test
  void literalsKeepTheirTextAsWritten() {
    BinaryExpression sum = (BinaryExpression) printlnArgument("println(1.50 + 'dijo \"hola\"');");
    NumberLiteral number = (NumberLiteral) sum.getLeft();
    StringLiteral string = (StringLiteral) sum.getRight();

    assertEquals(1.5, number.getValue());
    assertEquals("1.50", number.getLexeme());
    assertEquals("dijo \"hola\"", string.getValue());
    assertEquals("'dijo \"hola\"'", string.getLexeme());
  }

  @Test
  void nestedParenthesesAreNestedGroups() {
    GroupExpression outer = (GroupExpression) printlnArgument("println(((x)));");

    Identifier x = (Identifier) GroupExpression.unwrap(outer);
    assertEquals("x", x.getName());
    assertSame(x, GroupExpression.unwrap(x));
    // El paréntesis de la llamada no es un grupo: el argumento es ((x)), dos grupos.
    GroupExpression inner = (GroupExpression) outer.getExpression();
    assertSame(x, inner.getExpression());
  }
}
