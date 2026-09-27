package org.printscript.interpreter.output;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementación de {@link OutputProvider} que acumula las líneas en memoria. Pensada para tests y
 * para exponer la salida de una ejecución como resultado (ej. una respuesta HTTP).
 */
public class CollectingOutputProvider implements OutputProvider {

  private final List<String> lines = new ArrayList<>();

  @Override
  public void print(String line) {
    lines.add(line);
  }

  /** Devuelve una vista inmutable de las líneas emitidas hasta el momento. */
  public List<String> getLines() {
    return Collections.unmodifiableList(lines);
  }
}
