package org.printscript.interpreter;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
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
import org.printscript.interpreter.env.MapEnvProvider;
import org.printscript.interpreter.input.StdinInputProvider;
import org.printscript.interpreter.semantic.SemanticAnalyzerVisitor;

class ReadEnvTest {

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
  void readsStringEnvVarSuccessfully() {
    // let host: string = readEnv("HOST");
    MapEnvProvider envProvider = new MapEnvProvider(Map.of("HOST", "localhost"));
    InterpreterVisitor interpreter =
        new InterpreterVisitor(env, new StdinInputProvider(), envProvider, LanguageVersion.V1_1);

    CallExpression call =
        new CallExpression("readEnv", List.of(new StringLiteral("HOST", dummyPos)), dummyPos);
    VarDeclarationStatement decl =
        new VarDeclarationStatement(new Identifier("host", dummyPos), "string", call, dummyPos);

    decl.accept(interpreter);

    assertEquals("localhost", env.get("host"));
  }

  @Test
  void readsNumberEnvVarSuccessfully() {
    // let port: number = readEnv("PORT");
    MapEnvProvider envProvider = new MapEnvProvider(Map.of("PORT", "8080"));
    InterpreterVisitor interpreter =
        new InterpreterVisitor(env, new StdinInputProvider(), envProvider, LanguageVersion.V1_1);

    CallExpression call =
        new CallExpression("readEnv", List.of(new StringLiteral("PORT", dummyPos)), dummyPos);
    VarDeclarationStatement decl =
        new VarDeclarationStatement(new Identifier("port", dummyPos), "number", call, dummyPos);

    decl.accept(interpreter);

    assertEquals(8080.0, env.get("port"));
  }

  @Test
  void readsBooleanEnvVarSuccessfully() {
    // let debug: boolean = readEnv("DEBUG");
    MapEnvProvider envProvider = new MapEnvProvider(Map.of("DEBUG", "true"));
    InterpreterVisitor interpreter =
        new InterpreterVisitor(env, new StdinInputProvider(), envProvider, LanguageVersion.V1_1);

    CallExpression call =
        new CallExpression("readEnv", List.of(new StringLiteral("DEBUG", dummyPos)), dummyPos);
    VarDeclarationStatement decl =
        new VarDeclarationStatement(new Identifier("debug", dummyPos), "boolean", call, dummyPos);

    decl.accept(interpreter);

    assertEquals(true, env.get("debug"));
  }

  @Test
  void throwsOnUndefinedEnvVar() {
    // let missing: string = readEnv("NOT_FOUND");
    MapEnvProvider envProvider = new MapEnvProvider(Map.of());
    InterpreterVisitor interpreter =
        new InterpreterVisitor(env, new StdinInputProvider(), envProvider, LanguageVersion.V1_1);

    CallExpression call =
        new CallExpression("readEnv", List.of(new StringLiteral("NOT_FOUND", dummyPos)), dummyPos);
    VarDeclarationStatement decl =
        new VarDeclarationStatement(new Identifier("missing", dummyPos), "string", call, dummyPos);

    RuntimeException ex = assertThrows(RuntimeException.class, () -> decl.accept(interpreter));
    assertTrue(ex.getMessage().contains("Environment variable 'NOT_FOUND' is not defined"));
    assertTrue(ex.getMessage().contains(dummyPos.toString()));
  }

  @Test
  void throwsOnInvalidNumberCoercion() {
    MapEnvProvider envProvider = new MapEnvProvider(Map.of("PORT", "invalid_port"));
    InterpreterVisitor interpreter =
        new InterpreterVisitor(env, new StdinInputProvider(), envProvider, LanguageVersion.V1_1);

    CallExpression call =
        new CallExpression("readEnv", List.of(new StringLiteral("PORT", dummyPos)), dummyPos);
    VarDeclarationStatement decl =
        new VarDeclarationStatement(new Identifier("port", dummyPos), "number", call, dummyPos);

    RuntimeException ex = assertThrows(RuntimeException.class, () -> decl.accept(interpreter));
    assertTrue(ex.getMessage().contains("Cannot coerce env variable 'invalid_port' to number"));
    assertTrue(ex.getMessage().contains(dummyPos.toString()));
  }

  @Test
  void throwsOnInvalidBooleanCoercion() {
    MapEnvProvider envProvider = new MapEnvProvider(Map.of("FLAG", "Hola"));
    InterpreterVisitor interpreter =
        new InterpreterVisitor(env, new StdinInputProvider(), envProvider, LanguageVersion.V1_1);

    CallExpression call =
        new CallExpression("readEnv", List.of(new StringLiteral("FLAG", dummyPos)), dummyPos);
    VarDeclarationStatement decl =
        new VarDeclarationStatement(new Identifier("flag", dummyPos), "boolean", call, dummyPos);

    RuntimeException ex = assertThrows(RuntimeException.class, () -> decl.accept(interpreter));
    assertTrue(ex.getMessage().contains("Cannot coerce env variable 'Hola' to boolean"));
    assertTrue(ex.getMessage().contains(dummyPos.toString()));
  }

  @Test
  void readsEnvInsidePrintln() {
    MapEnvProvider envProvider = new MapEnvProvider(Map.of("GREETING", "Hello World"));
    InterpreterVisitor interpreter =
        new InterpreterVisitor(env, new StdinInputProvider(), envProvider, LanguageVersion.V1_1);

    CallExpression readCall =
        new CallExpression("readEnv", List.of(new StringLiteral("GREETING", dummyPos)), dummyPos);
    CallExpression printCall = new CallExpression("println", List.of(readCall), dummyPos);
    ExpressionStatement stmt = new ExpressionStatement(printCall, dummyPos);

    stmt.accept(interpreter);

    assertEquals("Hello World", outputStreamCaptor.toString().trim());
  }

  @Test
  void coercesInAssignmentStatement() {
    env.define("retries", "number", 0.0);
    MapEnvProvider envProvider = new MapEnvProvider(Map.of("MAX_RETRIES", "5"));
    InterpreterVisitor interpreter =
        new InterpreterVisitor(env, new StdinInputProvider(), envProvider, LanguageVersion.V1_1);

    CallExpression call =
        new CallExpression(
            "readEnv", List.of(new StringLiteral("MAX_RETRIES", dummyPos)), dummyPos);
    AssignmentStatement assign =
        new AssignmentStatement(new Identifier("retries", dummyPos), call, dummyPos);

    assign.accept(interpreter);

    assertEquals(5.0, env.get("retries"));
  }

  @Test
  void semanticAnalyzerAcceptsReadEnvInV1_1() {
    SemanticAnalyzerVisitor analyzer = new SemanticAnalyzerVisitor(env, LanguageVersion.V1_1);

    CallExpression call =
        new CallExpression("readEnv", List.of(new StringLiteral("API_URL", dummyPos)), dummyPos);
    VarDeclarationStatement decl =
        new VarDeclarationStatement(new Identifier("url", dummyPos), "string", call, dummyPos);

    assertDoesNotThrow(() -> decl.accept(analyzer));
  }

  @Test
  void semanticAnalyzerRejectsReadEnvInV1_0() {
    SemanticAnalyzerVisitor analyzer = new SemanticAnalyzerVisitor(env, LanguageVersion.V1_0);

    CallExpression call =
        new CallExpression("readEnv", List.of(new StringLiteral("API_URL", dummyPos)), dummyPos);
    VarDeclarationStatement decl =
        new VarDeclarationStatement(new Identifier("url", dummyPos), "string", call, dummyPos);

    RuntimeException ex = assertThrows(RuntimeException.class, () -> decl.accept(analyzer));
    assertTrue(ex.getMessage().contains("only supported in PrintScript 1.1"));
  }

  @Test
  void semanticAnalyzerRejectsNonStringArg() {
    SemanticAnalyzerVisitor analyzer = new SemanticAnalyzerVisitor(env, LanguageVersion.V1_1);

    CallExpression call =
        new CallExpression("readEnv", List.of(new NumberLiteral(42.0, dummyPos)), dummyPos);
    VarDeclarationStatement decl =
        new VarDeclarationStatement(new Identifier("url", dummyPos), "string", call, dummyPos);

    RuntimeException ex = assertThrows(RuntimeException.class, () -> decl.accept(analyzer));
    assertTrue(ex.getMessage().contains("readEnv argument must be of type string"));
  }

  @Test
  void semanticAnalyzerRejectsWrongArgCount() {
    SemanticAnalyzerVisitor analyzer = new SemanticAnalyzerVisitor(env, LanguageVersion.V1_1);

    CallExpression call = new CallExpression("readEnv", List.of(), dummyPos);
    VarDeclarationStatement decl =
        new VarDeclarationStatement(new Identifier("url", dummyPos), "string", call, dummyPos);

    RuntimeException ex = assertThrows(RuntimeException.class, () -> decl.accept(analyzer));
    assertTrue(ex.getMessage().contains("readEnv expects 1 argument"));
  }
}
