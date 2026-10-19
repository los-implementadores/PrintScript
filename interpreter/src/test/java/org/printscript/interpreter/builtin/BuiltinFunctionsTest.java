package org.printscript.interpreter.builtin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.printscript.common.LanguageVersion;
import org.printscript.common.Position;
import org.printscript.common.ast.CallExpression;
import org.printscript.common.ast.Expression;
import org.printscript.common.ast.ExpressionStatement;
import org.printscript.common.ast.Identifier;
import org.printscript.common.ast.NumberLiteral;
import org.printscript.common.ast.Statement;
import org.printscript.common.ast.StringLiteral;
import org.printscript.common.ast.VarDeclarationStatement;
import org.printscript.common.env.Environment;
import org.printscript.interpreter.ExecutionIo;
import org.printscript.interpreter.InterpreterVisitor;
import org.printscript.interpreter.env.MapEnvProvider;
import org.printscript.interpreter.input.ProgrammaticInputProvider;
import org.printscript.interpreter.output.CollectingOutputProvider;
import org.printscript.interpreter.semantic.SemanticAnalyzerVisitor;
import org.printscript.interpreter.semantic.SemanticException;

class BuiltinFunctionsTest {

  private static final Position POS = new Position(1, 1, 1, 10);

  private static CallExpression call(String callee, String... arguments) {
    List<Expression> args =
        Arrays.stream(arguments).map(a -> (Expression) new StringLiteral(a, POS)).toList();
    return new CallExpression(callee, args, POS);
  }

  private static IllegalStateException error(String detail) {
    return new IllegalStateException(detail);
  }

  @Test
  void rejectsUnknownFunction() {
    IllegalStateException e =
        assertThrows(
            IllegalStateException.class,
            () ->
                BuiltinFunctions.standard()
                    .resolve(call("foo", "x"), LanguageVersion.V1_1, BuiltinFunctionsTest::error));
    assertEquals("Unknown function 'foo'", e.getMessage());
  }

  @Test
  void rejectsFunctionNewerThanTheVersion() {
    IllegalStateException e =
        assertThrows(
            IllegalStateException.class,
            () ->
                BuiltinFunctions.standard()
                    .resolve(
                        call("readInput", "x"), LanguageVersion.V1_0, BuiltinFunctionsTest::error));
    assertEquals("Function 'readInput' is only supported in PrintScript 1.1.", e.getMessage());
  }

  @Test
  void rejectsWrongNumberOfArguments() {
    IllegalStateException e =
        assertThrows(
            IllegalStateException.class,
            () ->
                BuiltinFunctions.standard()
                    .resolve(call("println"), LanguageVersion.V1_0, BuiltinFunctionsTest::error));
    assertEquals("println expects 1 argument.", e.getMessage());
  }

  /** Una función nueva sólo se registra: el semántico y el intérprete no cambian. */
  @Test
  void newFunctionWorksWithoutChangingTheVisitors() {
    BuiltinFunctions builtins = new BuiltinFunctions(List.of(new PrintlnFunction(), new Upper()));
    // let s: string = upper("hola"); println(s);
    List<Statement> program =
        List.of(
            new VarDeclarationStatement(
                new Identifier("s", POS), "string", call("upper", "hola"), POS),
            new ExpressionStatement(
                new CallExpression("println", List.of(new Identifier("s", POS)), POS), POS));
    CollectingOutputProvider output = new CollectingOutputProvider();
    ExecutionIo io =
        new ExecutionIo(new ProgrammaticInputProvider(), new MapEnvProvider(Map.of()), output);
    SemanticAnalyzerVisitor semantic =
        new SemanticAnalyzerVisitor(new Environment(), builtins, LanguageVersion.V1_0);
    InterpreterVisitor interpreter =
        new InterpreterVisitor(new Environment(), io, builtins, LanguageVersion.V1_0);

    for (Statement statement : program) {
      statement.accept(semantic);
      statement.accept(interpreter);
    }

    assertEquals(List.of("HOLA"), output.getLines());
  }

  @Test
  void newFunctionTypeChecksItsArguments() {
    BuiltinFunctions builtins = new BuiltinFunctions(List.of(new Upper()));
    SemanticAnalyzerVisitor semantic =
        new SemanticAnalyzerVisitor(new Environment(), builtins, LanguageVersion.V1_0);
    Statement statement =
        new ExpressionStatement(
            new CallExpression("upper", List.of(new NumberLiteral(1.0, POS)), POS), POS);

    SemanticException e = assertThrows(SemanticException.class, () -> statement.accept(semantic));
    assertEquals(
        "Semantic Error at " + POS + ": upper expects a string, found number", e.getMessage());
  }

  /** {@code upper(s)}: función de ejemplo que sólo existe en este test. */
  private static final class Upper implements BuiltinFunction {

    @Override
    public String name() {
      return "upper";
    }

    @Override
    public LanguageVersion since() {
      return LanguageVersion.V1_0;
    }

    @Override
    public int arity() {
      return 1;
    }

    @Override
    public String checkTypes(TypeChecker checker, CallExpression call) {
      String type = checker.typeOf(call.getArguments().get(0), "string");
      if (!"string".equals(type)) {
        throw new SemanticException("upper expects a string, found " + type, call.getPosition());
      }
      return "string";
    }

    @Override
    public Object call(CallContext context, CallExpression call) {
      return ((String) context.evaluate(call.getArguments().get(0), "string")).toUpperCase();
    }
  }
}
