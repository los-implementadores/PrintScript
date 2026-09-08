package org.printscript.interpreter;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.printscript.common.Position;
import org.printscript.common.ast.*;
import org.printscript.common.env.Environment;
import org.printscript.interpreter.semantic.SemanticAnalyzerVisitor;

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
  void throwsWhenReassigningConstant() {
    // const pi: number = 3; pi = 4;
    VarDeclarationStatement decl =
        new VarDeclarationStatement(
            new Identifier("pi", dummyPos),
            "number",
            new NumberLiteral(3.0, dummyPos),
            true,
            dummyPos);
    decl.accept(analyzer);

    AssignmentStatement reassign =
        new AssignmentStatement(
            new Identifier("pi", dummyPos), new NumberLiteral(4.0, dummyPos), dummyPos);

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> reassign.accept(analyzer));
    assertTrue(exception.getMessage().contains("Cannot reassign constant 'pi'"));
  }

  @Test
  void allowsReadingConstant() {
    // const pi: number = 3; let r: number = pi;
    VarDeclarationStatement decl =
        new VarDeclarationStatement(
            new Identifier("pi", dummyPos),
            "number",
            new NumberLiteral(3.0, dummyPos),
            true,
            dummyPos);
    decl.accept(analyzer);

    VarDeclarationStatement read =
        new VarDeclarationStatement(
            new Identifier("r", dummyPos), "number", new Identifier("pi", dummyPos), dummyPos);

    assertDoesNotThrow(() -> read.accept(analyzer));
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

  @Test
  void acceptsIfWithBooleanCondition() {
    // if (true) { }
    IfStatement ifStmt =
        new IfStatement(new BooleanLiteral(true, dummyPos), java.util.List.of(), null, dummyPos);
    assertDoesNotThrow(() -> ifStmt.accept(analyzer));
  }

  @Test
  void throwsOnNonBooleanIfCondition() {
    // if (5) { }
    IfStatement ifStmt =
        new IfStatement(new NumberLiteral(5.0, dummyPos), java.util.List.of(), null, dummyPos);
    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> ifStmt.accept(analyzer));
    assertTrue(exception.getMessage().contains("if condition must be boolean"));
  }
}
