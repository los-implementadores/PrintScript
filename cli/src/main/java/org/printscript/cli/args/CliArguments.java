package org.printscript.cli.args;

import java.util.Optional;

/** Invocación cruda de la CLI: qué operación, sobre qué archivo y con qué opciones. */
public record CliArguments(
    String operation, String filePath, Optional<String> configPath, String versionLabel) {

  public static final String DEFAULT_VERSION = "1.0";
}
