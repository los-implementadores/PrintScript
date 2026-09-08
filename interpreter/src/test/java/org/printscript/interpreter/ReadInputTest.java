package org.printscript.interpreter;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.printscript.common.LanguageVersion;
import org.printscript.common.Position;
import org.printscript.common.ast.AssignmentStatement;
import org.printscript.common.ast.CallExpression;
import org.printscript.common.ast.ExpressionStatement;
import org.printscript.common.ast.Identifier;
import org.printscript.common.ast.NumberLiteral;
import org.printscript.common.ast.StringLiteral;
import org.printscript.common.ast.VarDeclarationStatement;
import org.printscript.common.env.Environment;

class ReadInputTest {

  private Environment env;
  private final Position dummyPos = new Position(1, 1, 1, 20);
  private final PrintStream standardOut = System.out;
  private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

  @BeforeEach
  void setUp() {
    env = new Environment();
    System.setOut(new PrintStream(outputStreamCaptor));
  }

  @AfterEach
  void tearDown() {
    System.setOut(standardOut);
  }

  @Test
  void readsNumberSuccessfully() {
    // let n: number = readInput("Edad: ");
    ProgrammaticInputProvider provider = new ProgrammaticInputProvider("25");
    InterpreterVisitor interpreter = new InterpreterVisitor(env, provider, LanguageVersion.V1_1);

    CallExpression call =
        new CallExpression("readInput", List.of(new StringLiteral("Edad: ", dummyPos)), dummyPos);
    VarDeclarationStatement decl =
        new VarDeclarationStatement(new Identifier("n", dummyPos), "number", call, dummyPos);

    decl.accept(interpreter);

    assertEquals(25.0, env.get("n"));
    assertTrue(outputStreamCaptor.toString().contains("Edad: "));
  }

  @Test
  void readsStringSuccessfully() {
    // let name: string = readInput("Nombre: ");
    ProgrammaticInputProvider provider = new ProgrammaticInputProvider("Juan");
    InterpreterVisitor interpreter = new InterpreterVisitor(env, provider, LanguageVersion.V1_1);

    CallExpression call =
        new CallExpression("readInput", List.of(new StringLiteral("Nombre: ", dummyPos)), dummyPos);
    VarDeclarationStatement decl =
        new VarDeclarationStatement(new Identifier("name", dummyPos), "string", call, dummyPos);

    decl.accept(interpreter);

    assertEquals("Juan", env.get("name"));
    assertTrue(outputStreamCaptor.toString().contains("Nombre: "));
  }

  @Test
  void readsBooleanSuccessfully() {
    // let active: boolean = readInput("Activo?: ");
    ProgrammaticInputProvider provider = new ProgrammaticInputProvider("true");
    InterpreterVisitor interpreter = new InterpreterVisitor(env, provider, LanguageVersion.V1_1);

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
    InterpreterVisitor interpreter = new InterpreterVisitor(env, provider, LanguageVersion.V1_1);

    CallExpression call =
        new CallExpression("readInput", List.of(new StringLiteral("Edad: ", dummyPos)), dummyPos);
    VarDeclarationStatement decl =
        new VarDeclarationStatement(new Identifier("n", dummyPos), "number", call, dummyPos);

    RuntimeException ex = assertThrows(RuntimeException.class, () -> decl.accept(interpreter));
    assertTrue(ex.getMessage().contains("Cannot coerce input 'no_es_numero' to number"));
    assertTrue(ex.getMessage().contains(dummyPos.toString()));
  }

  @Test
  void throwsOnInvalidBooleanCoercion() {
    // let b: boolean = readInput("Acepta?: "); con entrada "Hola" (ejemplo de la consigna)
    ProgrammaticInputProvider provider = new ProgrammaticInputProvider("Hola");
    InterpreterVisitor interpreter = new InterpreterVisitor(env, provider, LanguageVersion.V1_1);

    CallExpression call =
        new CallExpression(
            "readInput", List.of(new StringLiteral("Acepta?: ", dummyPos)), dummyPos);
    VarDeclarationStatement decl =
        new VarDeclarationStatement(new Identifier("b", dummyPos), "boolean", call, dummyPos);

    RuntimeException ex = assertThrows(RuntimeException.class, () -> decl.accept(interpreter));
    assertTrue(ex.getMessage().contains("Cannot coerce input 'Hola' to boolean"));
    assertTrue(ex.getMessage().contains(dummyPos.toString()));
  }

  @Test
  void coercesInAssignmentStatement() {
    // let x: number = 0; x = readInput("Nuevo valor: ");
    env.define("x", "number", 0.0);
    ProgrammaticInputProvider provider = new ProgrammaticInputProvider("99.5");
    InterpreterVisitor interpreter = new InterpreterVisitor(env, provider, LanguageVersion.V1_1);

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
    InterpreterVisitor interpreter = new InterpreterVisitor(env, provider, LanguageVersion.V1_1);

    CallExpression readCall =
        new CallExpression("readInput", List.of(new StringLiteral("Eco: ", dummyPos)), dummyPos);
    CallExpression printCall = new CallExpression("println", List.of(readCall), dummyPos);
    ExpressionStatement stmt = new ExpressionStatement(printCall, dummyPos);

    stmt.accept(interpreter);

    String output = outputStreamCaptor.toString();
    assertTrue(output.contains("Eco: "));
    assertTrue(output.contains("Mensaje de prueba"));
  }

  @Test
  void semanticAnalyzerAcceptsReadInputInV1_1() {
    SemanticAnalyzerVisitor analyzer = new SemanticAnalyzerVisitor(env, LanguageVersion.V1_1);

    CallExpression call =
        new CallExpression("readInput", List.of(new StringLiteral("Prompt: ", dummyPos)), dummyPos);
    VarDeclarationStatement decl =
        new VarDeclarationStatement(new Identifier("n", dummyPos), "number", call, dummyPos);

    assertDoesNotThrow(() -> decl.accept(analyzer));
  }

  @Test
  void semanticAnalyzerRejectsReadInputInV1_0() {
    SemanticAnalyzerVisitor analyzer = new SemanticAnalyzerVisitor(env, LanguageVersion.V1_0);

    CallExpression call =
        new CallExpression("readInput", List.of(new StringLiteral("Prompt: ", dummyPos)), dummyPos);
    VarDeclarationStatement decl =
        new VarDeclarationStatement(new Identifier("n", dummyPos), "number", call, dummyPos);

    RuntimeException ex = assertThrows(RuntimeException.class, () -> decl.accept(analyzer));
    assertTrue(ex.getMessage().contains("only supported in PrintScript 1.1"));
  }

  @Test
  void semanticAnalyzerRejectsNonStringPrompt() {
    SemanticAnalyzerVisitor analyzer = new SemanticAnalyzerVisitor(env, LanguageVersion.V1_1);

    CallExpression call =
        new CallExpression("readInput", List.of(new NumberLiteral(123.0, dummyPos)), dummyPos);
    VarDeclarationStatement decl =
        new VarDeclarationStatement(new Identifier("n", dummyPos), "number", call, dummyPos);

    RuntimeException ex = assertThrows(RuntimeException.class, () -> decl.accept(analyzer));
    assertTrue(ex.getMessage().contains("readInput argument must be of type string"));
  }

  @Test
  void semanticAnalyzerRejectsWrongArgumentCount() {
    SemanticAnalyzerVisitor analyzer = new SemanticAnalyzerVisitor(env, LanguageVersion.V1_1);

    CallExpression noArgs = new CallExpression("readInput", List.of(), dummyPos);
    VarDeclarationStatement decl =
        new VarDeclarationStatement(new Identifier("n", dummyPos), "number", noArgs, dummyPos);

    RuntimeException ex = assertThrows(RuntimeException.class, () -> decl.accept(analyzer));
    assertTrue(ex.getMessage().contains("readInput expects 1 argument"));
  }
}
