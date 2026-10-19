package org.printscript.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.printscript.common.LanguageVersion;
import org.printscript.common.configs.AnalyzerConfig;
import org.printscript.common.linterViolations.Violation;
import org.printscript.formatter.FormattingRules;
import org.printscript.interpreter.ExecutionIo;
import org.printscript.interpreter.env.MapEnvProvider;
import org.printscript.interpreter.input.ProgrammaticInputProvider;
import org.printscript.interpreter.output.CollectingOutputProvider;

class RunnerServicesTest {

  private final LanguageServices services = LanguageServices.printScript();

  @Test
  void executionCollectsOutputWithoutTouchingStdout() {
    CollectingOutputProvider output = new CollectingOutputProvider();
    ExecutionIo io =
        new ExecutionIo(new ProgrammaticInputProvider(), new MapEnvProvider(Map.of()), output);

    services
        .execution()
        .execute(new StringReader("let a: number = 1 + 2; println(a);"), LanguageVersion.V1_0, io);

    assertEquals(List.of("3"), output.getLines());
  }

  @Test
  void executionRoutesReadInputPromptToOutput() {
    CollectingOutputProvider output = new CollectingOutputProvider();
    ExecutionIo io =
        new ExecutionIo(new ProgrammaticInputProvider("Ana"), new MapEnvProvider(Map.of()), output);

    services
        .execution()
        .execute(
            new StringReader("let n: string = readInput(\"Nombre: \"); println(n);"),
            LanguageVersion.V1_1,
            io);

    assertEquals(List.of("Nombre: ", "Ana"), output.getLines());
  }

  @Test
  void validationFailsOnSyntaxError() {
    assertThrows(
        RuntimeException.class,
        () -> services.validation().validate(new StringReader("let = ;"), LanguageVersion.V1_0));
  }

  @Test
  void analysisReturnsViolations() {
    AnalyzerConfig config = new AnalyzerConfig();
    config.activeComplexPrintln = true;

    List<Violation> violations =
        services
            .analysis()
            .analyze(new StringReader("println(1 + 2);"), LanguageVersion.V1_0, config);

    assertEquals(1, violations.size());
  }

  @Test
  void formattingReturnsFormattedSource() {
    String formatted =
        services
            .formatting()
            .format(
                new StringReader("let x:number=1;"), LanguageVersion.V1_0, new FormattingRules());

    assertTrue(formatted.contains("let x"));
  }
}
