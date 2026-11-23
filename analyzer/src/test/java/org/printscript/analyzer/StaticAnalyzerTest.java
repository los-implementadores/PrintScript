package org.printscript.analyzer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.printscript.analyzer.TestAst.POS;
import static org.printscript.analyzer.TestAst.call;
import static org.printscript.analyzer.TestAst.declaration;
import static org.printscript.analyzer.TestAst.ifElse;
import static org.printscript.analyzer.TestAst.println;
import static org.printscript.analyzer.TestAst.sum;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.printscript.analyzer.rule.LintRules;
import org.printscript.analyzer.rule.NodeRule;
import org.printscript.analyzer.rule.ProgramRule;
import org.printscript.analyzer.violation.Violation;
import org.printscript.ast.CallExpression;
import org.printscript.ast.Statement;
import org.printscript.ast.VarDeclarationStatement;
import org.printscript.rules.RuleConfig;
import org.printscript.source.Position;

/** El recorrido del linter, sin importar qué reglas tenga. */
class StaticAnalyzerTest {

  private static Iterator<Statement> program(Statement... statements) {
    return List.of(statements).iterator();
  }

  private static List<String> messages(List<Violation> violations) {
    return violations.stream().map(Violation::message).toList();
  }

  /** Reporta cada declaración de una variable llamada x. */
  private static final NodeRule<VarDeclarationStatement> FORBID_X =
      new NodeRule<>() {
        @Override
        public Class<VarDeclarationStatement> nodeType() {
          return VarDeclarationStatement.class;
        }

        @Override
        public List<Violation> check(VarDeclarationStatement declaration) {
          return "x".equals(declaration.getName().getName())
              ? List.of(new Violation("Do not call it x.", declaration.getPosition()))
              : List.of();
        }
      };

  /** Reporta cada llamada, para ver a qué nodos llega una regla. */
  private static final NodeRule<CallExpression> EVERY_CALL =
      new NodeRule<>() {
        @Override
        public Class<CallExpression> nodeType() {
          return CallExpression.class;
        }

        @Override
        public List<Violation> check(CallExpression call) {
          return List.of(new Violation("Call to " + call.getCallee() + ".", call.getPosition()));
        }
      };

  /** Cuenta las sentencias del programa con un fold: lo que sabe es un número. */
  private static final ProgramRule<Integer> COUNT_STATEMENTS =
      new ProgramRule<>() {
        @Override
        public Integer start() {
          return 0;
        }

        @Override
        public Integer after(Integer known, Statement statement) {
          return known + 1;
        }

        @Override
        public List<Violation> violations(Integer known) {
          return List.of(new Violation(known + " statements.", POS));
        }
      };

  @Test
  void aCustomRuleRunsWithoutChangingTheAnalyzer() {
    StaticAnalyzer analyzer = new StaticAnalyzerImpl(new LintRules(List.of(FORBID_X), List.of()));

    List<Violation> violations =
        analyzer.analyze(program(declaration("x", POS), declaration("y", POS)));

    assertEquals(List.of("Do not call it x."), messages(violations));
  }

  @Test
  void aNodeRuleGetsEveryNodeOfItsTypeAndNoOther() {
    StaticAnalyzer analyzer = new StaticAnalyzerImpl(new LintRules(List.of(EVERY_CALL), List.of()));

    List<Violation> violations =
        analyzer.analyze(
            program(declaration("x", POS), ifElse(List.of(println("x")), List.of(println("x")))));

    assertEquals(List.of("Call to println.", "Call to println."), messages(violations));
  }

  @Test
  void aProgramRuleSeesEveryStatementAndReportsAtTheEnd() {
    StaticAnalyzer analyzer =
        new StaticAnalyzerImpl(new LintRules(List.of(), List.of(COUNT_STATEMENTS)));

    List<Violation> violations =
        analyzer.analyze(program(declaration("a", POS), declaration("b", POS), println("a")));

    assertEquals(List.of("3 statements."), messages(violations));
  }

  @Test
  void theSameAnalyzerAnalyzesSeveralProgramsWithoutMixingThem() {
    StaticAnalyzer analyzer =
        new StaticAnalyzerImpl(RuleConfig.of(Map.of("activeUnusedVariables", true)));

    List<Violation> first = analyzer.analyze(program(declaration("unused", POS)));
    List<Violation> second = analyzer.analyze(program(declaration("used", POS), println("used")));
    List<Violation> firstAgain = analyzer.analyze(program(declaration("unused", POS)));

    assertEquals(1, first.size());
    assertTrue(second.isEmpty());
    assertEquals(first, firstAgain);
  }

  @Test
  void violationsComeSortedByPositionWhateverRuleFoundThem() {
    RuleConfig config =
        RuleConfig.of(
            Map.of(
                "identifier_format", "snake case",
                "activeComplexPrintln", true,
                "activeUnusedVariables", true));
    StaticAnalyzer analyzer = new StaticAnalyzerImpl(config);

    List<Violation> violations =
        analyzer.analyze(
            program(
                declaration("myVar", new Position(1, 5, 1, 9)),
                declaration("other", new Position(2, 5, 2, 9)),
                call("println", TestAst.plusOne("myVar"), new Position(3, 1, 3, 15))));

    assertEquals(
        List.of(
            "Variable 'myVar' should be in snake_case.",
            "Variable 'other' is declared but never used.",
            "Complex expression in 'println'. Consider extracting to an intermediate variable."),
        messages(violations));
  }

  @Test
  void anEmptyConfigActivatesNoRules() {
    assertTrue(LintRules.from(RuleConfig.empty()).isEmpty());
    assertTrue(
        new StaticAnalyzerImpl(RuleConfig.empty())
            .analyze(program(declaration("my_var", POS), call("println", sum())))
            .isEmpty());
  }
}
