package org.printscript.interpreter.builtin;

import org.printscript.common.Position;
import org.printscript.interpreter.ExecutionException;
import org.printscript.interpreter.ExecutionIo;

/** {@code readInput(prompt)}: muestra el prompt y lee una línea de la entrada del programa. */
final class ReadInputFunction extends ExternalTextFunction {

  @Override
  public String name() {
    return "readInput";
  }

  @Override
  protected String read(ExecutionIo io, String prompt, Position position) {
    io.output().print(prompt);
    String raw = io.input().readInput(prompt);
    if (raw == null) {
      throw new ExecutionException("End of input reached while reading input.", position);
    }
    return raw;
  }

  @Override
  protected String sourceDescription() {
    return "input";
  }
}
