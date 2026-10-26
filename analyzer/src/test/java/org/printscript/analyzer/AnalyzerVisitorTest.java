package org.printscript.analyzer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.printscript.analyzer.config.AnalyzerConfig;
import org.printscript.analyzer.config.NamingConvention;
import org.printscript.analyzer.violation.Severity;
import org.printscript.analyzer.violation.Violation;
import org.printscript.common.Position;
import org.printscript.common.ast.AssignmentStatement;
import org.printscript.common.ast.BinaryExpression;
import org.printscript.common.ast.BooleanLiteral;
import org.printscript.common.ast.CallExpression;
import org.printscript.common.ast.Expression;
import org.printscript.common.ast.ExpressionStatement;
import org.printscript.common.ast.Identifier;
import org.printscript.common.ast.IfStatement;
import org.printscript.common.ast.NumberLiteral;
import org.printscript.common.ast.Statement;
import org.printscript.common.ast.StringLiteral;
import org.printscript.common.ast.VarDeclarationStatement;

class AnalyzerVisitorTest {

  private AnalyzerConfig config;
  private Position defaultPos;

  @BeforeEach
  void setUp() {
    config = new AnalyzerConfig(); // Resetea la config antes de cada test
    defaultPos = new Position(1, 1, 1, 10);
  }

  // --- REGLA 1: NAMING CONVENTION ---

  @Test
  void reportsAVariableThatIsNotInCamelCase() {
    config.activeNamingConvention = true;
    config.namingConventionFormat = NamingConvention.CAMEL_CASE;

    config.activeUnusedVariables = false; // lo apago para los tests

    AnalyzerVisitor visitor = new AnalyzerVisitor(config);

    // Simulamos: let my_variable: number = "mati";
    Identifier id = new Identifier("my_variable", defaultPos);
    Expression initializer = new StringLiteral("mati", defaultPos);
    VarDeclarationStatement stmt =
        new VarDeclarationStatement(id, "number", initializer, defaultPos);

    stmt.accept(visitor);
    List<Violation> violations = visitor.getFinalViolations();

    assertEquals(1, violations.size());
    assertTrue(violations.get(0).getMessage().contains("camelCase"));
    assertEquals(Severity.WARNING, violations.get(0).getSeverity());
  }

  @Test
  void acceptsSnakeCaseWhenThatIsTheConfiguredConvention() {
    config.activeNamingConvention = true;
    config.namingConventionFormat = NamingConvention.SNAKE_CASE;

    config.activeUnusedVariables = false; // lo apago para tests

    AnalyzerVisitor visitor = new AnalyzerVisitor(config);

    // Simulamos: let my_variable: number = "mati";
    Identifier id = new Identifier("my_variable", defaultPos);
    Expression initializer = new StringLiteral("mati", defaultPos);
    VarDeclarationStatement stmt =
        new VarDeclarationStatement(id, "number", initializer, defaultPos);

    stmt.accept(visitor);

    List<Violation> violations = visitor.getFinalViolations();

    assertTrue(violations.isEmpty());
  }

  // --- REGLA 2: UNUSED VARIABLES ---

  @Test
  void warnsAboutAVariableThatIsNeverRead() {
    config.activeUnusedVariables = true;

    config.activeNamingConvention = false;
    AnalyzerVisitor visitor = new AnalyzerVisitor(config);

    // Simulamos: let x: number = 5;
    Identifier id = new Identifier("x", defaultPos);
    VarDeclarationStatement stmt =
        new VarDeclarationStatement(id, "number", new NumberLiteral(5, defaultPos), defaultPos);

    stmt.accept(visitor);

    // Obtenemos violaciones finales
    List<Violation> violations = visitor.getFinalViolations();

    assertEquals(1, violations.size());
    assertTrue(violations.get(0).getMessage().contains("never used"));
  }

  @Test
  void doesNotWarnAboutAVariableThatIsRead() {
    config.activeUnusedVariables = true;

    config.activeNamingConvention = false;
    AnalyzerVisitor visitor = new AnalyzerVisitor(config);

    Identifier id = new Identifier("x", defaultPos);

    // 1. Declaramos la variable
    VarDeclarationStatement declStmt = new VarDeclarationStatement(id, "number", null, defaultPos);
    declStmt.accept(visitor);

    // 2. Usamos la variable (simulamos un println(x) o una reasignación x = 2)
    AssignmentStatement assignStmt =
        new AssignmentStatement(id, new NumberLiteral(2, defaultPos), defaultPos);
    assignStmt.accept(visitor);

    List<Violation> violations = visitor.getFinalViolations();

    assertTrue(violations.isEmpty());
  }

  @Test
  void warnsAboutAnOuterVariableWhenOnlyTheOneDeclaredInsideTheBlockIsRead() {
    config.activeUnusedVariables = true;
    AnalyzerVisitor visitor = new AnalyzerVisitor(config);
    Position outer = new Position(1, 1, 1, 20);

    declaration("x", outer).accept(visitor);
    ifElse(List.of(declaration("x", new Position(2, 3, 2, 22)), println("x")), null)
        .accept(visitor);

    List<Violation> violations = visitor.getFinalViolations();
    assertEquals(1, violations.size());
    assertEquals(outer, violations.get(0).getPosition());
  }

  @Test
  void warnsAboutAVariableOfOneBranchWhenOnlyTheOtherBranchReadsTheSameName() {
    config.activeUnusedVariables = true;
    AnalyzerVisitor visitor = new AnalyzerVisitor(config);
    Position thenBranch = new Position(2, 3, 2, 22);

    ifElse(
            List.of(declaration("y", thenBranch)),
            List.of(declaration("y", new Position(4, 3, 4, 22)), println("y")))
        .accept(visitor);

    List<Violation> violations = visitor.getFinalViolations();
    assertEquals(1, violations.size());
    assertEquals(thenBranch, violations.get(0).getPosition());
  }

  @Test
  void doesNotWarnAboutAnOuterVariableReadInsideABlock() {
    config.activeUnusedVariables = true;
    AnalyzerVisitor visitor = new AnalyzerVisitor(config);

    declaration("x", defaultPos).accept(visitor);
    ifElse(List.of(println("x")), null).accept(visitor);

    assertTrue(visitor.getFinalViolations().isEmpty());
  }

  private VarDeclarationStatement declaration(String name, Position position) {
    return new VarDeclarationStatement(
        new Identifier(name, position), "number", new NumberLiteral(1, position), position);
  }

  private Statement println(String name) {
    return new ExpressionStatement(
        new CallExpression("println", List.of(new Identifier(name, defaultPos)), defaultPos),
        defaultPos);
  }

  private IfStatement ifElse(List<Statement> thenBlock, List<Statement> elseBlock) {
    return new IfStatement(new BooleanLiteral(true, defaultPos), thenBlock, elseBlock, defaultPos);
  }

  // --- REGLA 3: COMPLEX PRINTLN ---

  @Test
  void reportsAnExpressionPassedToPrintln() {
    config.activeComplexPrintln = true;
    AnalyzerVisitor visitor = new AnalyzerVisitor(config);

    // Simulamos: println(2 + 2)
    BinaryExpression complexExpr =
        new BinaryExpression(
            new NumberLiteral(2, defaultPos), "+", new NumberLiteral(2, defaultPos), defaultPos);
    CallExpression printlnStmt = new CallExpression("println", List.of(complexExpr), defaultPos);

    printlnStmt.accept(visitor);
    List<Violation> violations = visitor.getFinalViolations();

    assertEquals(1, violations.size());
    assertTrue(violations.get(0).getMessage().contains("Complex expression in 'println'"));
  }

  @Test
  void acceptsALiteralOrIdentifierPassedToPrintln() {
    config.activeComplexPrintln = true;
    AnalyzerVisitor visitor = new AnalyzerVisitor(config);

    // Simulamos: println("Hola")
    StringLiteral simpleExpr = new StringLiteral("Hola", defaultPos);
    CallExpression printlnStmt = new CallExpression("println", List.of(simpleExpr), defaultPos);

    printlnStmt.accept(visitor);
    List<Violation> violations = visitor.getFinalViolations();

    assertTrue(violations.isEmpty());
  }

  @Test
  void aViolationPrintsSeverityMessageAndPosition() {
    Position pos = new Position(1, 5, 1, 15);
    Violation violation = new Violation("Variable should be camelCase", Severity.WARNING, pos);

    assertEquals("Variable should be camelCase", violation.getMessage());
    assertEquals(Severity.WARNING, violation.getSeverity());
    assertEquals(pos, violation.getPosition());

    assertEquals("[WARNING] Variable should be camelCase at 1:5-1:15", violation.toString());
  }

  @Test
  void reportsAnExpressionPassedToReadInput() {
    config.activeComplexReadInput = true;
    AnalyzerVisitor visitor = new AnalyzerVisitor(config);

    // readInput("Enter " + "name")
    BinaryExpression complexExpr =
        new BinaryExpression(
            new StringLiteral("Enter ", defaultPos),
            "+",
            new StringLiteral("name", defaultPos),
            defaultPos);
    CallExpression readInputStmt =
        new CallExpression("readInput", List.of(complexExpr), defaultPos);

    readInputStmt.accept(visitor);
    List<Violation> violations = visitor.getFinalViolations();

    assertEquals(1, violations.size());
    assertTrue(violations.get(0).getMessage().contains("readInput"));
  }

  @Test
  void acceptsALiteralPassedToReadInput() {
    config.activeComplexReadInput = true;
    AnalyzerVisitor visitor = new AnalyzerVisitor(config);

    // readInput("Enter name: ")
    StringLiteral prompt = new StringLiteral("Enter name: ", defaultPos);
    CallExpression readInputStmt = new CallExpression("readInput", List.of(prompt), defaultPos);

    readInputStmt.accept(visitor);
    List<Violation> violations = visitor.getFinalViolations();

    assertTrue(violations.isEmpty());
  }

  @Test
  void readsTheTckRuleNamesAndLeavesRulesOffByDefault() {
    String json =
        """
        {
          "identifier_format": "snake case",
          "mandatory-variable-or-literal-in-println": true,
          "mandatory-variable-or-literal-in-readInput": true
        }
        """;
    AnalyzerConfig parsed = AnalyzerConfig.fromJson(new java.io.StringReader(json));

    assertTrue(parsed.activeNamingConvention);
    assertEquals(NamingConvention.SNAKE_CASE, parsed.namingConventionFormat);
    assertTrue(parsed.activeComplexPrintln);
    assertTrue(parsed.activeComplexReadInput);
    assertFalse(parsed.activeUnusedVariables);

    // Empty config should have no rules active
    AnalyzerConfig empty = AnalyzerConfig.fromJson(new java.io.StringReader("{}"));
    assertFalse(empty.activeNamingConvention);
    assertFalse(empty.activeComplexPrintln);
    assertFalse(empty.activeComplexReadInput);
    assertFalse(empty.activeUnusedVariables);
  }
}
