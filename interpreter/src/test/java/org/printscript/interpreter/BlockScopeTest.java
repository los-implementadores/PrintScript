package org.printscript.interpreter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.printscript.common.LanguageVersion;
import org.printscript.common.Position;
import org.printscript.common.ast.AssignmentStatement;
import org.printscript.common.ast.BooleanLiteral;
import org.printscript.common.ast.CallExpression;
import org.printscript.common.ast.ExpressionStatement;
import org.printscript.common.ast.Identifier;
import org.printscript.common.ast.IfStatement;
import org.printscript.common.ast.NumberLiteral;
import org.printscript.common.ast.Statement;
import org.printscript.common.ast.VarDeclarationStatement;
import org.printscript.common.env.Environment;
import org.printscript.interpreter.builtin.BuiltinFunctions;
import org.printscript.interpreter.env.MapEnvProvider;
import org.printscript.interpreter.input.ProgrammaticInputProvider;
import org.printscript.interpreter.output.CollectingOutputProvider;
import org.printscript.interpreter.semantic.SemanticAnalyzerVisitor;
import org.printscript.interpreter.semantic.SemanticException;

/**
 * Las llaves de un {@code if} abren un scope, como en TypeScript: lo que se declara adentro no
 * existe afuera, y el chequeo semántico tiene que coincidir con lo que pasa al ejecutar.
 */
class BlockScopeTest {

  private static final Position POS = new Position(1, 1, 1, 10);

  private final Environment memory = new Environment();
  private final CollectingOutputProvider output = new CollectingOutputProvider();
  private final SemanticAnalyzerVisitor semantic =
      new SemanticAnalyzerVisitor(
          new Environment(), BuiltinFunctions.standard(), LanguageVersion.V1_1);
  private final InterpreterVisitor interpreter =
      new InterpreterVisitor(
          memory,
          new ExecutionIo(new ProgrammaticInputProvider(), new MapEnvProvider(Map.of()), output),
          BuiltinFunctions.standard(),
          LanguageVersion.V1_1);

  /** Analiza y ejecuta cada sentencia, en ese orden, como hace el runner. */
  private void run(Statement... program) {
    for (Statement statement : program) {
      statement.accept(semantic);
      statement.accept(interpreter);
    }
  }

  private static Statement let(String name, double value) {
    return new VarDeclarationStatement(
        new Identifier(name, POS), "number", new NumberLiteral(value, POS), POS);
  }

  private static Statement assign(String name, double value) {
    return new AssignmentStatement(new Identifier(name, POS), new NumberLiteral(value, POS), POS);
  }

  private static Statement println(String name) {
    return new ExpressionStatement(
        new CallExpression("println", List.of(new Identifier(name, POS)), POS), POS);
  }

  private static Statement ifElse(boolean condition, List<Statement> then, List<Statement> other) {
    return new IfStatement(new BooleanLiteral(condition, POS), then, other, POS);
  }

  @Test
  void aVariableDeclaredInABlockDoesNotExistAfterIt() {
    Statement useAfterBlock = println("x");
    run(ifElse(false, List.of(let("x", 1)), null));

    SemanticException e =
        assertThrows(SemanticException.class, () -> useAfterBlock.accept(semantic));
    assertEquals("Semantic Error at " + POS + ": Variable 'x' is not declared.", e.getMessage());
  }

  @Test
  void bothBranchesCanDeclareTheSameName() {
    Statement program =
        ifElse(true, List.of(let("y", 1), println("y")), List.of(let("y", 2), println("y")));

    assertDoesNotThrow(() -> run(program));
    assertEquals(List.of("1"), output.getLines());
  }

  @Test
  void aBlockReadsAndAssignsVariablesFromOutside() {
    run(let("a", 1), ifElse(true, List.of(assign("a", 2), println("a")), null), println("a"));

    assertEquals(List.of("2", "2"), output.getLines());
  }

  @Test
  void aDeclarationInsideABlockShadowsTheOuterOne() {
    run(let("x", 1), ifElse(true, List.of(let("x", 2), println("x")), null), println("x"));

    assertEquals(List.of("2", "1"), output.getLines());
  }

  @Test
  void aBlockVariableDoesNotLeakIntoTheOuterMemory() {
    run(ifElse(true, List.of(let("y", 1)), null));

    assertFalse(memory.isDeclared("y"));
  }
}
