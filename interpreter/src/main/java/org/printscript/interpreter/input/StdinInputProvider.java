package org.printscript.interpreter.input;

import java.util.Scanner;

/**
 * Implementación de {@link InputProvider} que lee líneas desde standard input ({@code System.in}).
 * Utilizada por defecto en la CLI interactiva.
 */
public class StdinInputProvider implements InputProvider {

  private final Scanner scanner;

  public StdinInputProvider() {
    this(new Scanner(System.in));
  }

  public StdinInputProvider(Scanner scanner) {
    this.scanner = scanner;
  }

  @Override
  public String readInput(String prompt) {
    if (scanner.hasNextLine()) {
      return scanner.nextLine();
    }
    return null;
  }
}
