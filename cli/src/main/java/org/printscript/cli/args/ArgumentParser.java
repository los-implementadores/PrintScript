package org.printscript.cli.args;

import java.util.Optional;

/**
 * Parsea {@code <operation> <file> [--config <path>] [--version <1.0|1.1>]}.
 *
 * <p>Devuelve vacío si no hay suficientes argumentos (lo que dispara el modo interactivo).
 */
public final class ArgumentParser {

  private ArgumentParser() {}

  public static Optional<CliArguments> parse(String[] args) {
    if (args.length < 2) {
      return Optional.empty();
    }
    String configPath = null;
    String versionLabel = CliArguments.DEFAULT_VERSION;

    for (int i = 2; i < args.length - 1; i++) {
      if ("--config".equals(args[i])) {
        configPath = args[i + 1];
      } else if ("--version".equals(args[i])) {
        versionLabel = args[i + 1];
      }
    }
    return Optional.of(
        new CliArguments(args[0], args[1], Optional.ofNullable(configPath), versionLabel));
  }
}
