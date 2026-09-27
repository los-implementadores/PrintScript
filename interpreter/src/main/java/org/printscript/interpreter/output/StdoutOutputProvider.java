package org.printscript.interpreter.output;

/** Implementación de {@link OutputProvider} que escribe cada línea en {@code System.out}. */
public class StdoutOutputProvider implements OutputProvider {

  @Override
  public void print(String line) {
    System.out.println(line);
  }
}
