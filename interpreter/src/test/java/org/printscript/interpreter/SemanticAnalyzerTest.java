package org.printscript.interpreter;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.printscript.common.Position;
import org.printscript.common.ast.*;
import org.printscript.common.env.Environment;

class SemanticAnalyzerTest {

  private Environment env;
  private SemanticAnalyzerVisitor analyzer;
  private final Position dummyPos = new Position(1, 1, 1, 10);

  @BeforeEach
  void setUp() {
    env = new Environment();
    analyzer = new SemanticAnalyzerVisitor(env);
  }

  @Test
  void acceptsValidNumberDeclaration() {
    // let x: number = 5;
    VarDeclarationStatement stmt =
        new VarDeclarationStatement(
            new Identifier("x", dummyPos), "number", new NumberLiteral(5.0, dummyPos), dummyPos);

    assertDoesNotThrow(() -> stmt.accept(analyzer));
    assertTrue(env.isDeclared("x"));
  }

  @Test
  void throwsOnTypeMismatchInDeclaration() {
    // let age: number = "veinte";
    VarDeclarationStatement stmt =
        new VarDeclarationStatement(
            new Identifier("age", dummyPos),
            "number",
            new StringLiteral("veinte", dummyPos),
            dummyPos);

    RuntimeException exception = assertThrows(RuntimeException.class, () -> stmt.accept(analyzer));
    assertTrue(exception.getMessage().contains("Cannot assign string to variable of type number"));
  }

  @Test
  void throwsOnUndeclaredVariableUsage() {
    // x = 10; (sin haber hecho el let)
    AssignmentStatement stmt =
        new AssignmentStatement(
            new Identifier("x", dummyPos), new NumberLiteral(10.0, dummyPos), dummyPos);

    RuntimeException exception = assertThrows(RuntimeException.class, () -> stmt.accept(analyzer));
    assertTrue(exception.getMessage().contains("Variable 'x' is not declared"));
  }

  @Test
  void acceptsValidBooleanDeclaration() {
    // let ok: boolean = true;
    VarDeclarationStatement stmt =
        new VarDeclarationStatement(
            new Identifier("ok", dummyPos),
            "boolean",
            new BooleanLiteral(true, dummyPos),
            dummyPos);

    assertDoesNotThrow(() -> stmt.accept(analyzer));
    assertTrue(env.isDeclared("ok"));
  }

  @Test
  void throwsWhenAssigningNumberToBoolean() {
    // let ok: boolean = 5;
    VarDeclarationStatement stmt =
        new VarDeclarationStatement(
            new Identifier("ok", dummyPos), "boolean", new NumberLiteral(5.0, dummyPos), dummyPos);

    RuntimeException exception = assertThrows(RuntimeException.class, () -> stmt.accept(analyzer));
    assertTrue(exception.getMessage().contains("Cannot assign number to variable of type boolean"));
  }

  @Test
  void throwsOnInvalidMathOperation() {
    // "hola" - 5
    BinaryExpression expr =
        new BinaryExpression(
            new StringLiteral("hola", dummyPos), "-", new NumberLiteral(5.0, dummyPos), dummyPos);

    RuntimeException exception = assertThrows(RuntimeException.class, () -> expr.accept(analyzer));
    assertTrue(exception.getMessage().contains("Operator '-' requires numeric operands"));
  }
}
