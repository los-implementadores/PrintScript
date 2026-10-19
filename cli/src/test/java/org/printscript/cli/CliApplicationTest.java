package org.printscript.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Reader;
import java.io.StringReader;
import java.nio.file.NoSuchFileException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import org.junit.jupiter.api.Test;
import org.printscript.cli.io.Console;
import org.printscript.cli.io.FileAccess;

class CliApplicationTest {

  private final FakeConsole console = new FakeConsole();
  private final InMemoryFiles files = new InMemoryFiles();
  private final CliApplication app = new CliApplication(console, files);

  @Test
  void unknownOperationExitsWithError() {
    int code = app.run(new String[] {"foo", "script.ps"});

    assertEquals(CliApplication.EXIT_ERROR, code);
    assertTrue(console.err().contains("Unsupported operation: foo"));
  }

  @Test
  void missingFileExitsWithErrorBeforePrintingProgress() {
    int code = app.run(new String[] {"execution", "nope.ps"});

    assertEquals(CliApplication.EXIT_ERROR, code);
    assertTrue(console.err().contains("File not found: nope.ps"));
    assertFalse(console.out().contains("Ejecutando script..."));
  }

  @Test
  void invalidLinterConfigExitsWithError() {
    files.put("script.ps", "println(1);");
    files.put("rules.json", "{ not json");

    int code = app.run(new String[] {"analyzing", "script.ps", "--config", "rules.json"});

    assertEquals(CliApplication.EXIT_ERROR, code);
  }

  @Test
  void validationReportsSemanticErrors() {
    files.put("script.ps", "let x: number = \"hola\";");

    int code = app.run(new String[] {"validation", "script.ps"});

    assertEquals(CliApplication.EXIT_ERROR, code);
    assertTrue(console.err().contains("Semantic Error"));
  }

  @Test
  void interactiveModeSharesInputWithReadInput() {
    files.put("script.ps", "let n: string = readInput(\"Nombre: \");\nprintln(\"Hola \" + n);");
    console.feed("3", "script.ps", "1.1", "Matias");

    int code = app.run(new String[] {});

    assertEquals(CliApplication.EXIT_OK, code);
    assertTrue(console.out().contains("Hola Matias"));
  }

  private static final class FakeConsole implements Console {
    private final Queue<String> input = new ArrayDeque<>();
    private final List<String> out = new ArrayList<>();
    private final List<String> err = new ArrayList<>();

    void feed(String... lines) {
      input.addAll(Arrays.asList(lines));
    }

    String out() {
      return String.join("\n", out);
    }

    String err() {
      return String.join("\n", err);
    }

    @Override
    public void println(String line) {
      out.add(line);
    }

    @Override
    public void print(String text) {
      out.add(text);
    }

    @Override
    public void error(String line) {
      err.add(line);
    }

    @Override
    public String readLine() {
      return input.poll();
    }
  }

  private static final class InMemoryFiles implements FileAccess {
    private final Map<String, String> contents = new HashMap<>();

    void put(String path, String content) {
      contents.put(path, content);
    }

    @Override
    public Reader openReader(String path) throws NoSuchFileException {
      String content = contents.get(path);
      if (content == null) {
        throw new NoSuchFileException(path);
      }
      return new StringReader(content);
    }

    @Override
    public void write(String path, String content) {
      contents.put(path, content);
    }
  }
}
