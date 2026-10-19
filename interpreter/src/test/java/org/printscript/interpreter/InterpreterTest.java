package org.printscript.interpreter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.printscript.common.LanguageVersion;
import org.printscript.common.Position;
import org.printscript.common.ast.*;
import org.printscript.common.env.Environment;
import org.printscript.interpreter.builtin.BuiltinFunctions;
import org.printscript.interpreter.env.MapEnvProvider;
import org.printscript.interpreter.input.ProgrammaticInputProvider;
import org.printscript.interpreter.output.CollectingOutputProvider;

class InterpreterTest {

  private Environment env;
  private InterpreterVisitor interpreter;
  private CollectingOutputProvider output;
  private final Position dummyPos = new Position(1, 1, 1, 10);

  @BeforeEach
  void setUp() {
    env = new Environment();
    output = new CollectingOutputProvider();
    ExecutionIo io =
        new ExecutionIo(new ProgrammaticInputProvider(), new MapEnvProvider(Map.of()), output);
    interpreter =
        new InterpreterVisitor(env, io, BuiltinFunctions.standard(), LanguageVersion.V1_0);
  }

  @Test
  void executesMathOperationsCorrectly() {
    // let a: number = 10;
    new VarDeclarationStatement(
            new Identifier("a", dummyPos), "number", new NumberLiteral(10.0, dummyPos), dummyPos)
        .accept(interpreter);

    // let b: number = a / 2;
    new VarDeclarationStatement(
            new Identifier("b", dummyPos),
            "number",
            new BinaryExpression(
                new Identifier("a", dummyPos), "/", new NumberLiteral(2.0, dummyPos), dummyPos),
            dummyPos)
        .accept(interpreter);

    // a tiene que valer 10.0 y b tiene que valer 5.0 en el environment
    assertEquals(10.0, env.get("a"));
    assertEquals(5.0, env.get("b"));
  }

  @Test
  void executesStringConcatenationAndPrints() {
    // println("Joe" + " " + "Doe");
    BinaryExpression concat1 =
        new BinaryExpression(
            new StringLiteral("Joe", dummyPos), "+", new StringLiteral(" ", dummyPos), dummyPos);
    BinaryExpression concat2 =
        new BinaryExpression(concat1, "+", new StringLiteral("Doe", dummyPos), dummyPos);

    CallExpression printlnCall = new CallExpression("println", List.of(concat2), dummyPos);
    ExpressionStatement stmt = new ExpressionStatement(printlnCall, dummyPos);

    stmt.accept(interpreter);

    // Verificamos que salió por consola correctamente
    assertEquals(List.of("Joe Doe"), output.getLines());
  }

  @Test
  void printsNumbersWithoutDecimalsIfInteger() {
    // println(42);
    CallExpression printlnCall =
        new CallExpression("println", List.of(new NumberLiteral(42.0, dummyPos)), dummyPos);
    ExpressionStatement stmt = new ExpressionStatement(printlnCall, dummyPos);

    stmt.accept(interpreter);

    // El formatter interno del intérprete debería sacarle el .0
    assertEquals(List.of("42"), output.getLines());
  }

  @Test
  void declareVariable() {
    VarDeclarationStatement decl =
        new VarDeclarationStatement(new Identifier("x", dummyPos), "number", null, dummyPos);
    decl.accept(interpreter);

    assertTrue(env.isDeclared("x"));
    assertEquals("number", env.getType("x"));
    assertNull(env.get("x"));

    AssignmentStatement assign =
        new AssignmentStatement(
            new Identifier("x", dummyPos), new NumberLiteral(10.0, dummyPos), dummyPos);
    assign.accept(interpreter);

    assertEquals(10.0, env.get("x"));
  }
}
