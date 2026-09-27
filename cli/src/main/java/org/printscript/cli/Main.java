package org.printscript.cli;

import org.printscript.cli.io.LocalFileAccess;
import org.printscript.cli.io.SystemConsole;

/** Entry point: conecta la CLI a la consola y el filesystem reales. */
public final class Main {

  private Main() {}

  public static void main(String[] args) {
    int exitCode = new CliApplication(new SystemConsole(), new LocalFileAccess()).run(args);
    if (exitCode != CliApplication.EXIT_OK) {
      System.exit(exitCode);
    }
  }
}
