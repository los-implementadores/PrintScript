package org.printscript.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.printscript.common.LanguageVersion;
import org.printscript.interpreter.ExecutionIo;
import org.printscript.interpreter.env.MapEnvProvider;
import org.printscript.interpreter.input.ProgrammaticInputProvider;
import org.printscript.interpreter.output.CollectingOutputProvider;
import org.printscript.interpreter.semantic.SemanticException;

/**
 * Los dos casos en que el chequeo semántico no coincidía con la ejecución porque los bloques no
 * tenían scope propio.
 */
class BlockScopeExecutionTest {

  private final ExecutionService execution = LanguageServices.printScript().execution();
  private final CollectingOutputProvider output = new CollectingOutputProvider();
  private final ExecutionIo io =
      new ExecutionIo(new ProgrammaticInputProvider(), new MapEnvProvider(Map.of()), output);

  private void execute(String source) {
    execution.execute(new StringReader(source), LanguageVersion.V1_1, io);
  }

  @Test
  void usingAVariableFromABranchThatDidNotRunFailsTheSemanticCheck() {
    SemanticException e =
        assertThrows(
            SemanticException.class,
            () -> execute("if (false) { let x: number = 1; }\nprintln(x);"));

    assertTrue(e.getMessage().endsWith("Variable 'x' is not declared."));
    assertEquals(List.of(), output.getLines());
  }

  @Test
  void bothBranchesCanDeclareTheSameVariable() {
    execute("if (true) { let y: number = 1; println(y); } else { let y: number = 2; println(y); }");

    assertEquals(List.of("1"), output.getLines());
  }
}
