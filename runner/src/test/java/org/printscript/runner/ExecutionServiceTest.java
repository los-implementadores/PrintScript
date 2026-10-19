package org.printscript.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.printscript.common.LanguageVersion;
import org.printscript.common.Position;
import org.printscript.common.ast.EagerProgram;
import org.printscript.common.ast.ExpressionStatement;
import org.printscript.common.ast.NumberLiteral;
import org.printscript.common.ast.Statement;
import org.printscript.interpreter.semantic.SemanticException;

/**
 * {@link ExecutionService} con dobles: no hace falta lexer, parser ni intérprete reales para probar
 * cómo orquesta las etapas.
 */
class ExecutionServiceTest {

  private static final Position POS = new Position(1, 1, 1, 1);

  private final Statement first = new ExpressionStatement(new NumberLiteral(1.0, POS), POS);
  private final Statement second = new ExpressionStatement(new NumberLiteral(2.0, POS), POS);
  private final Map<Statement, String> names = Map.of(first, "1", second, "2");
  private final List<String> calls = new ArrayList<>();

  private final ProgramLoader loader =
      (source, version) -> new EagerProgram(List.of(first, second), POS);
  private final InterpreterFactory interpreters =
      (version, io) -> statement -> calls.add("execute " + names.get(statement));

  @Test
  void analyzesEachStatementRightBeforeExecutingIt() {
    SemanticAnalyzerFactory semantic =
        version -> statement -> calls.add("analyze " + names.get(statement));

    new ExecutionService(loader, semantic, interpreters)
        .execute(new StringReader(""), LanguageVersion.V1_0, null);

    assertEquals(List.of("analyze 1", "execute 1", "analyze 2", "execute 2"), calls);
  }

  @Test
  void doesNotExecuteAStatementThatFailsTheSemanticCheck() {
    SemanticAnalyzerFactory semantic =
        version ->
            statement -> {
              calls.add("analyze " + names.get(statement));
              if ("2".equals(names.get(statement))) {
                throw new SemanticException("tipo inválido", POS);
              }
            };
    ExecutionService service = new ExecutionService(loader, semantic, interpreters);

    assertThrows(
        SemanticException.class,
        () -> service.execute(new StringReader(""), LanguageVersion.V1_0, null));
    assertEquals(List.of("analyze 1", "execute 1", "analyze 2"), calls);
  }
}
