package org.printscript.cli.command;

import java.io.IOException;
import java.io.Reader;
import org.printscript.cli.io.Console;
import org.printscript.cli.io.FileAccess;
import org.printscript.formatter.FormattingRules;
import org.printscript.runner.FormattingService;

/** Formatea el script y sobreescribe el archivo original. */
public class FormattingCommand implements Command {

  private final FormattingService service;
  private final Console console;
  private final FileAccess files;

  public FormattingCommand(FormattingService service, Console console, FileAccess files) {
    this.service = service;
    this.console = console;
    this.files = files;
  }

  @Override
  public String name() {
    return "formatting";
  }

  @Override
  public boolean acceptsConfig() {
    return true;
  }

  @Override
  public void execute(CommandRequest request) throws IOException {
    console.println("Formateando archivo...");
    FormattingRules rules = loadRules(request);

    String formatted;
    try (Reader source = files.openReader(request.filePath())) {
      formatted = service.format(source, request.version(), rules);
    }
    files.write(request.filePath(), formatted);
    console.println("Formatting successful. El archivo ha sido formateado.");
  }

  private FormattingRules loadRules(CommandRequest request) throws IOException {
    if (request.configPath().isEmpty()) {
      return new FormattingRules();
    }
    try (Reader reader = files.openReader(request.configPath().get())) {
      return FormattingRules.fromJson(reader);
    }
  }
}
