package org.printscript.cli;

import org.printscript.cli.io.LocalFileAccess;
import org.printscript.cli.io.SystemConsole;
import org.printscript.interpreter.env.SystemEnvProvider;
import org.printscript.runner.LanguageServices;

/**
 * Entry point: conecta la CLI a la consola, el filesystem y las variables de entorno reales, y a
 * los servicios de PrintScript. Es el único lugar de la CLI que elige implementaciones concretas.
 */
public final class Main {

  private Main() {}

  public static void main(String[] args) {
    CliApplication cli =
        new CliApplication(
            new SystemConsole(),
            new LocalFileAccess(),
            new SystemEnvProvider(),
            LanguageServices.printScript());
    int exitCode = cli.run(args);
    if (exitCode != CliApplication.EXIT_OK) {
      System.exit(exitCode);
    }
  }
}
