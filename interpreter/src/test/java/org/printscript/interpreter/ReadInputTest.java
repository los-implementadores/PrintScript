package org.printscript.interpreter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.printscript.ast.AssignmentStatement;
import org.printscript.ast.CallExpression;
import org.printscript.ast.ExpressionStatement;
import org.printscript.ast.Identifier;
import org.printscript.ast.NumberLiteral;
import org.printscript.ast.StringLiteral;
import org.printscript.ast.VarDeclarationStatement;
import org.printscript.interpreter.builtin.BuiltinFunctions;
import org.printscript.interpreter.builtin.PrintlnFunction;
import org.printscript.interpreter.env.MapEnvProvider;
import org.printscript.interpreter.input.InputProvider;
import org.printscript.interpreter.input.ProgrammaticInputProvider;
import org.printscript.interpreter.output.CollectingOutputProvider;
import org.printscript.interpreter.scope.Environment;
import org.printscript.interpreter.semantic.SemanticAnalyzerVisitor;
import org.printscript.interpreter.semantic.SemanticException;
import org.printscript.source.Position;

class ReadInputTest {

  private Environment env;
  private CollectingOutputProvider output;
  private final Position dummyPos = new Position(1, 1, 1, 20);

  @BeforeEach
  void setUp() {
    env = new Environment();
    output = new CollectingOutputProvider();
  }

  private InterpreterVisitor interpreterWith(InputProvider input) {
    ExecutionIo io = new ExecutionIo(input, new MapEnvProvider(Map.of()), output);
    return new InterpreterVisitor(env, io, BuiltinFunctions.standard());
  }

  @Test
  void readsNumberSuccessfully() {
    // let n: number = readInput("Edad: ");
    ProgrammaticInputProvider provider = new ProgrammaticInputProvider("25");
    InterpreterVisitor interpreter = interpreterWith(provider);

    CallExpression call =
        new CallExpression("readInput", List.of(new StringLiteral("Edad: ", dummyPos)), dummyPos);
    VarDeclarationStatement decl =
        new VarDeclarationStatement(new Identifier("n", dummyPos), "number", call, dummyPos);

    decl.accept(interpreter);

    assertEquals(25.0, env.get("n"));
    assertTrue(output.getLines().contains("Edad: "));
  }

  @Test
  void readsStringSuccessfully() {
    // let name: string = readInput("Nombre: ");
    ProgrammaticInputProvider provider = new ProgrammaticInputProvider("Juan");
    InterpreterVisitor interpreter = interpreterWith(provider);

    CallExpression call =
        new CallExpression("readInput", List.of(new StringLiteral("Nombre: ", dummyPos)), dummyPos);
    VarDeclarationStatement decl =
        new VarDeclarationStatement(new Identifier("name", dummyPos), "string", call, dummyPos);

    decl.accept(interpreter);

    assertEquals("Juan", env.get("name"));
    assertTrue(output.getLines().contains("Nombre: "));
  }

  @Test
  void readsBooleanSuccessfully() {
    // let active: boolean = readInput("Activo?: ");
    ProgrammaticInputProvider provider = new ProgrammaticInputProvider("true");
    InterpreterVisitor interpreter = interpreterWith(provider);

    CallExpression call =
        new CallExpression(
            "readInput", List.of(new StringLiteral("Activo?: ", dummyPos)), dummyPos);
    VarDeclarationStatement decl =
        new VarDeclarationStatement(new Identifier("active", dummyPos), "boolean", call, dummyPos);

    decl.accept(interpreter);

    assertEquals(true, env.get("active"));
  }

  @Test
  void throwsOnInvalidNumberCoercion() {
    // let n: number = readInput("Edad: "); con entrada "no_es_numero"
    ProgrammaticInputProvider provider = new ProgrammaticInputProvider("no_es_numero");
    InterpreterVisitor interpreter = interpreterWith(provider);

    CallExpression call =
        new CallExpression("readInput", List.of(new StringLiteral("Edad: ", dummyPos)), dummyPos);
    VarDeclarationStatement decl =
        new VarDeclarationStatement(new Identifier("n", dummyPos), "number", call, dummyPos);

    RuntimeException ex = assertThrows(ExecutionException.class, () -> decl.accept(interpreter));
    assertTrue(ex.getMessage().contains("Cannot coerce input 'no_es_numero' to number"));
    assertTrue(ex.getMessage().contains(dummyPos.toString()));
  }

  @Test
  void throwsOnInvalidBooleanCoercion() {
    // let b: boolean = readInput("Acepta?: "); con entrada "Hola" (ejemplo de la consigna)
    ProgrammaticInputProvider provider = new ProgrammaticInputProvider("Hola");
    InterpreterVisitor interpreter = interpreterWith(provider);

    CallExpression call =
        new CallExpression(
            "readInput", List.of(new StringLiteral("Acepta?: ", dummyPos)), dummyPos);
    VarDeclarationStatement decl =
        new VarDeclarationStatement(new Identifier("b", dummyPos), "boolean", call, dummyPos);

    RuntimeException ex = assertThrows(ExecutionException.class, () -> decl.accept(interpreter));
    assertTrue(ex.getMessage().contains("Cannot coerce input 'Hola' to boolean"));
    assertTrue(ex.getMessage().contains(dummyPos.toString()));
  }

  @Test
  void coercesInAssignmentStatement() {
    // let x: number = 0; x = readInput("Nuevo valor: ");
    env.define("x", "number", 0.0);
    ProgrammaticInputProvider provider = new ProgrammaticInputProvider("99.5");
    InterpreterVisitor interpreter = interpreterWith(provider);

    CallExpression call =
        new CallExpression(
            "readInput", List.of(new StringLiteral("Nuevo valor: ", dummyPos)), dummyPos);
    AssignmentStatement assign =
        new AssignmentStatement(new Identifier("x", dummyPos), call, dummyPos);

    assign.accept(interpreter);

    assertEquals(99.5, env.get("x"));
  }

  @Test
  void readsInsidePrintln() {
    // println(readInput("Eco: "));
    ProgrammaticInputProvider provider = new ProgrammaticInputProvider("Mensaje de prueba");
    InterpreterVisitor interpreter = interpreterWith(provider);

    CallExpression readCall =
        new CallExpression("readInput", List.of(new StringLiteral("Eco: ", dummyPos)), dummyPos);
    CallExpression printCall = new CallExpression("println", List.of(readCall), dummyPos);
    ExpressionStatement stmt = new ExpressionStatement(printCall, dummyPos);

    stmt.accept(interpreter);

    assertEquals(List.of("Eco: ", "Mensaje de prueba"), output.getLines());
  }

  @Test
  void semanticAnalyzerAcceptsReadInput() {
    SemanticAnalyzerVisitor analyzer =
        new SemanticAnalyzerVisitor(env, BuiltinFunctions.standard());

    CallExpression call =
        new CallExpression("readInput", List.of(new StringLiteral("Prompt: ", dummyPos)), dummyPos);
    VarDeclarationStatement decl =
        new VarDeclarationStatement(new Identifier("n", dummyPos), "number", call, dummyPos);

    assertDoesNotThrow(() -> decl.accept(analyzer));
  }

  @Test
  void semanticAnalyzerRejectsReadInputWhenItIsNotAvailable() {
    SemanticAnalyzerVisitor analyzer =
        new SemanticAnalyzerVisitor(env, new BuiltinFunctions(List.of(new PrintlnFunction())));

    CallExpression call =
        new CallExpression("readInput", List.of(new StringLiteral("Prompt: ", dummyPos)), dummyPos);
    VarDeclarationStatement decl =
        new VarDeclarationStatement(new Identifier("n", dummyPos), "number", call, dummyPos);

    RuntimeException ex = assertThrows(SemanticException.class, () -> decl.accept(analyzer));
    assertTrue(ex.getMessage().contains("Unknown function 'readInput'"));
  }

  @Test
  void semanticAnalyzerRejectsNonStringPrompt() {
    SemanticAnalyzerVisitor analyzer =
        new SemanticAnalyzerVisitor(env, BuiltinFunctions.standard());

    CallExpression call =
        new CallExpression("readInput", List.of(new NumberLiteral(123.0, dummyPos)), dummyPos);
    VarDeclarationStatement decl =
        new VarDeclarationStatement(new Identifier("n", dummyPos), "number", call, dummyPos);

    RuntimeException ex = assertThrows(SemanticException.class, () -> decl.accept(analyzer));
    assertTrue(ex.getMessage().contains("readInput argument must be of type string"));
  }

  @Test
  void semanticAnalyzerRejectsWrongArgumentCount() {
    SemanticAnalyzerVisitor analyzer =
        new SemanticAnalyzerVisitor(env, BuiltinFunctions.standard());

    CallExpression noArgs = new CallExpression("readInput", List.of(), dummyPos);
    VarDeclarationStatement decl =
        new VarDeclarationStatement(new Identifier("n", dummyPos), "number", noArgs, dummyPos);

    RuntimeException ex = assertThrows(SemanticException.class, () -> decl.accept(analyzer));
    assertTrue(ex.getMessage().contains("readInput expects 1 argument"));
  }
}
