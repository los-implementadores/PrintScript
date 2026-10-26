package org.printscript.cli.command;

import java.io.IOException;

/**
 * Una operación de la CLI. Cada comando adapta un servicio de {@code org.printscript.runner} a la
 * terminal: abre los archivos, invoca el servicio y muestra el resultado.
 */
public interface Command {

  /** Nombre con el que se invoca el comando (ej. {@code "execution"}). */
  String name();

  /** Si el comando acepta un JSON de configuración vía {@code --config}. */
  boolean acceptsConfig();

  void execute(CommandRequest request) throws IOException;
}
