package org.printscript.cli.command;

import java.io.IOException;
import java.io.Reader;
import org.printscript.cli.io.Console;
import org.printscript.cli.io.FileAccess;
import org.printscript.interpreter.env.SystemEnvProvider;
import org.printscript.runner.ExecutionIo;
import org.printscript.runner.ExecutionService;

public class ExecutionCommand implements Command {

  private final ExecutionService service;
  private final Console console;
  private final FileAccess files;

  public ExecutionCommand(ExecutionService service, Console console, FileAccess files) {
    this.service = service;
    this.console = console;
    this.files = files;
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
    ExecutionIo io =
        new ExecutionIo(prompt -> console.readLine(), new SystemEnvProvider(), console::println);
    try (Reader source = files.openReader(request.filePath())) {
      service.execute(source, request.version(), io);
    }
    console.println("\nExecution finished.");
  }
}
