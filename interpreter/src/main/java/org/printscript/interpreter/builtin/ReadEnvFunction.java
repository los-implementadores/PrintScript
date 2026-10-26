package org.printscript.interpreter.builtin;

import org.printscript.common.Position;
import org.printscript.interpreter.ExecutionException;
import org.printscript.interpreter.ExecutionIo;

/** {@code readEnv(name)}: lee una variable de entorno. */
final class ReadEnvFunction extends ExternalTextFunction {

  @Override
  public String name() {
    return "readEnv";
  }

  @Override
  protected String read(ExecutionIo io, String variableName, Position position) {
    String raw = io.env().getEnv(variableName);
    if (raw == null) {
      throw new ExecutionException(
          "Environment variable '" + variableName + "' is not defined.", position);
    }
    return raw;
  }

  @Override
  protected String sourceDescription() {
    return "env variable";
  }
}
