package org.printscript.cli.command;

import java.io.IOException;
import java.io.Reader;
import org.printscript.cli.io.Console;
import org.printscript.cli.io.FileAccess;
import org.printscript.runner.ValidationService;

public class ValidationCommand implements Command {

  private final ValidationService service;
  private final Console console;
  private final FileAccess files;

  public ValidationCommand(ValidationService service, Console console, FileAccess files) {
    this.service = service;
    this.console = console;
    this.files = files;
  }

  @Override
  public String name() {
    return "validation";
  }

  @Override
  public boolean acceptsConfig() {
    return false;
  }

  @Override
  public void execute(CommandRequest request) throws IOException {
    console.println("Validando sintaxis del archivo...");
    try (Reader source = files.openReader(request.filePath())) {
      service.validate(source, request.version());
    }
    console.println("Validation successful. (Sin errores de sintaxis)");
  }
}
