package org.printscript.interpreter.input;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

/**
 * Implementación de {@link InputProvider} que provee valores programáticamente desde una cola.
 * Diseñada para tests automáticos y ejecuciones desatendidas.
 */
public class ProgrammaticInputProvider implements InputProvider {

  private final Queue<String> inputs;

  public ProgrammaticInputProvider(List<String> inputs) {
    this.inputs = new ArrayDeque<>(inputs);
  }

  public ProgrammaticInputProvider(String... inputs) {
    this(Arrays.asList(inputs));
  }

  @Override
  public String readInput(String prompt) {
    return inputs.poll();
  }
}
