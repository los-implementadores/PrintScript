package org.printscript.analyzer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.printscript.analyzer.LintRulesTest.analyze;
import static org.printscript.analyzer.TestAst.POS;
import static org.printscript.analyzer.TestAst.declaration;
import static org.printscript.analyzer.TestAst.ifElse;
import static org.printscript.analyzer.TestAst.println;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.printscript.analyzer.violation.Violation;
import org.printscript.ast.AssignmentStatement;
import org.printscript.ast.Identifier;
import org.printscript.ast.NumberLiteral;
import org.printscript.ast.Statement;
import org.printscript.source.Position;

/** La regla de variables sin usar, con los scopes de los bloques. */
class UnusedVariablesTest {

  @Test
  void warnsAboutAVariableThatIsNeverRead() {
    List<Violation> violations =
        analyze(Map.of("activeUnusedVariables", true), declaration("x", POS));

    assertEquals(1, violations.size());
    assertEquals("Variable 'x' is declared but never used.", violations.get(0).getMessage());
  }

  @Test
  void aReadOrAReassignmentCountsAsAUse() {
    Map<String, Object> config = Map.of("activeUnusedVariables", true);
    Statement reassignment =
        new AssignmentStatement(new Identifier("x", POS), new NumberLiteral(2, POS), POS);

    assertTrue(analyze(config, declaration("x", POS), println("x")).isEmpty());
    assertTrue(analyze(config, declaration("x", POS), reassignment).isEmpty());
  }

  @Test
  void warnsAboutAnOuterVariableWhenOnlyTheOneDeclaredInsideTheBlockIsRead() {
    Position outer = new Position(1, 1, 1, 20);

    List<Violation> violations =
        analyze(
            Map.of("activeUnusedVariables", true),
            declaration("x", outer),
            ifElse(List.of(declaration("x", new Position(2, 3, 2, 22)), println("x")), null));

    assertEquals(1, violations.size());
    assertEquals(outer, violations.get(0).getPosition());
  }

  @Test
  void warnsAboutAVariableOfOneBranchWhenOnlyTheOtherBranchReadsTheSameName() {
    Position thenBranch = new Position(2, 3, 2, 22);

    List<Violation> violations =
        analyze(
            Map.of("activeUnusedVariables", true),
            ifElse(
                List.of(declaration("y", thenBranch)),
                List.of(declaration("y", new Position(4, 3, 4, 22)), println("y"))));

    assertEquals(1, violations.size());
    assertEquals(thenBranch, violations.get(0).getPosition());
  }

  @Test
  void doesNotWarnAboutAnOuterVariableReadInsideABlock() {
    assertTrue(
        analyze(
                Map.of("activeUnusedVariables", true),
                declaration("x", POS),
                ifElse(List.of(println("x")), null))
            .isEmpty());
  }
}
