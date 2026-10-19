package org.printscript.cli.command;

import java.io.IOException;
import java.io.Reader;
import org.printscript.cli.io.Console;
import org.printscript.cli.io.FileAccess;
import org.printscript.interpreter.ExecutionIo;
import org.printscript.interpreter.env.EnvProvider;
import org.printscript.runner.ExecutionService;

public class ExecutionCommand implements Command {

  private final ExecutionService service;
  private final Console console;
  private final FileAccess files;
  private final EnvProvider environment;

  public ExecutionCommand(
      ExecutionService service, Console console, FileAccess files, EnvProvider environment) {
    this.service = service;
    this.console = console;
    this.files = files;
    this.environment = environment;
  }

  @Override
  public String name() {
    return "execution";
  }

  @Override
  public boolean acceptsConfig() {
    return false;
  }

  @Override
  public void execute(CommandRequest request) throws IOException {
    console.println("Ejecutando script...\n");
    // La salida y la entrada del programa se conectan a la misma consola que usa la CLI.
    ExecutionIo io = new ExecutionIo(prompt -> console.readLine(), environment, console::println);
    try (Reader source = files.openReader(request.filePath())) {
      service.execute(source, request.version(), io);
    }
    console.println("\nExecution finished.");
  }
}
