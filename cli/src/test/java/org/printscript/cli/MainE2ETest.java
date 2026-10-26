package org.printscript.cli;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.printscript.cli.io.LocalFileAccess;
import org.printscript.cli.io.SystemConsole;
import org.printscript.interpreter.env.MapEnvProvider;
import org.printscript.runner.LanguageServices;

/**
 * Corre la CLI de punta a punta sobre archivos reales, armada igual que en {@link Main} salvo por
 * los streams de la consola y las variables de entorno, que son de memoria. Así los tests no tocan
 * {@code System.out} ni {@code System.in} (estado global) y pueden correr en paralelo.
 */
class MainE2ETest {

  @TempDir Path tempDir;

  private final ByteArrayOutputStream out = new ByteArrayOutputStream();
  private final ByteArrayOutputStream err = new ByteArrayOutputStream();

  private int runCli(String stdin, Map<String, String> environment, String... args) {
    SystemConsole console =
        new SystemConsole(
            new PrintStream(out, true, UTF_8),
            new PrintStream(err, true, UTF_8),
            new ByteArrayInputStream(stdin.getBytes(UTF_8)));
    CliApplication cli =
        new CliApplication(
            console,
            new LocalFileAccess(),
            new MapEnvProvider(environment),
            LanguageServices.printScript());
    return cli.run(args);
  }

  private int runCli(String... args) {
    return runCli("", Map.of(), args);
  }

  private String output() {
    return out.toString(UTF_8);
  }

  private Path resource(String fileName) throws Exception {
    URL resource = getClass().getClassLoader().getResource(fileName);
    if (resource == null) {
      throw new IllegalArgumentException("No se encontró el archivo en resources: " + fileName);
    }
    return Paths.get(resource.toURI());
  }

  @Test
  void analyzingReportsAComplexExpressionInsidePrintln() throws Exception {
    String script = resource("analyzer_script.ps").toString();
    String config = resource("linter_rules.json").toString();

    int code = runCli("analyzing", script, "--config", config);

    assertEquals(CliApplication.EXIT_OK, code);
    assertTrue(output().contains("Linter found violations:"));
    assertTrue(output().contains("[WARNING] Complex expression in 'println'"));
  }

  @Test
  void formattingRewritesTheFileWithTheConfiguredRules() throws Exception {
    // Se formatea una copia para no modificar el archivo de resources.
    Path script = tempDir.resolve("format_test_copy.ps");
    Files.copy(resource("format_test.ps"), script);
    String config = resource("formatter_rules.json").toString();

    int code = runCli("formatting", script.toString(), "--config", config);

    assertEquals(CliApplication.EXIT_OK, code);
    assertEquals("let name: string = \"Joe\";\n\nprintln(name);", Files.readString(script).trim());
    assertTrue(output().contains("Formatting successful"));
  }

  @Test
  void executionPrintsTheProgramOutputBetweenTheCliMessages() throws Exception {
    int code = runCli("execution", resource("execution_script.ps").toString());

    assertEquals(CliApplication.EXIT_OK, code);
    String output = output();
    assertTrue(output.indexOf("Ejecutando script...") < output.indexOf("Result: 3"));
    assertTrue(output.indexOf("Result: 3") < output.indexOf("Execution finished."));
  }

  @Test
  void executionReadsUserInputFromTheConsoleInVersion11() throws Exception {
    String script = resource("read_input_script.ps").toString();

    int code = runCli("Matias\n25\n", Map.of(), "execution", script, "--version", "1.1");

    assertEquals(CliApplication.EXIT_OK, code);
    assertTrue(output().contains("Nombre: "));
    assertTrue(output().contains("Edad: "));
    assertTrue(output().contains("Hola Matias edad 25"));
  }

  @Test
  void executionReadsEnvironmentVariablesInVersion11() throws Exception {
    String script = resource("read_env_script.ps").toString();

    int code = runCli("", Map.of("PATH", "/usr/bin"), "execution", script, "--version", "1.1");

    assertEquals(CliApplication.EXIT_OK, code);
    assertTrue(output().contains("Path found"));
  }

  @Test
  void executionOfAMissingEnvironmentVariableExitsWithAnError() throws Exception {
    String script = resource("read_env_script.ps").toString();

    int code = runCli("", Map.of(), "execution", script, "--version", "1.1");

    assertEquals(CliApplication.EXIT_ERROR, code);
    assertTrue(err.toString(UTF_8).contains("Environment variable 'PATH' is not defined."));
  }
}
