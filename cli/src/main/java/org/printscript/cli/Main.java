package org.printscript.cli;

import org.printscript.cli.io.LocalFileAccess;
import org.printscript.cli.io.SystemConsole;
import org.printscript.interpreter.env.SystemEnvProvider;

/**
 * Entry point: conecta la CLI a la consola, el filesystem y las variables de entorno reales. Es el
 * único lugar de la CLI que elige implementaciones atadas al sistema.
 */
public final class Main {

  private Main() {}

  public static void main(String[] args) {
    int exitCode =
        new CliApplication(new SystemConsole(), new LocalFileAccess(), new SystemEnvProvider())
            .run(args);
    if (exitCode != CliApplication.EXIT_OK) {
      System.exit(exitCode);
    }
  }
}
