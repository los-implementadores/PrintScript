package org.printscript.cli;

import java.nio.file.NoSuchFileException;
import java.util.List;
import java.util.Optional;
import org.printscript.cli.args.ArgumentParser;
import org.printscript.cli.args.CliArguments;
import org.printscript.cli.command.AnalyzingCommand;
import org.printscript.cli.command.Command;
import org.printscript.cli.command.CommandRegistry;
import org.printscript.cli.command.CommandRequest;
import org.printscript.cli.command.ExecutionCommand;
import org.printscript.cli.command.FormattingCommand;
import org.printscript.cli.command.ValidationCommand;
import org.printscript.cli.interactive.InteractiveMenu;
import org.printscript.cli.io.Console;
import org.printscript.cli.io.FileAccess;
import org.printscript.common.LanguageVersion;
import org.printscript.runner.AnalysisService;
import org.printscript.runner.ExecutionService;
import org.printscript.runner.FormattingService;
import org.printscript.runner.ValidationService;

/**
 * Arma los comandos y despacha una invocación. No sabe nada del lenguaje (eso vive en {@code
 * org.printscript.runner}) ni de {@code System.*} (eso vive en {@code org.printscript.cli.io}).
 */
public class CliApplication {

  public static final int EXIT_OK = 0;
  public static final int EXIT_ERROR = 1;

  private final Console console;
  private final CommandRegistry registry;

  public CliApplication(Console console, FileAccess files) {
    this.console = console;
    this.registry =
        new CommandRegistry(
            List.of(
                new ValidationCommand(new ValidationService(), console, files),
                new AnalyzingCommand(new AnalysisService(), console, files),
                new ExecutionCommand(new ExecutionService(), console, files),
                new FormattingCommand(new FormattingService(), console, files)));
  }

  /** Corre la CLI y devuelve el exit code. */
  public int run(String[] args) {
    Optional<CliArguments> parsed = ArgumentParser.parse(args);
    if (parsed.isEmpty()) {
      parsed = new InteractiveMenu(console, registry).ask();
    }
    return parsed.map(this::dispatch).orElse(EXIT_OK);
  }

  private int dispatch(CliArguments arguments) {
    Optional<Command> command = registry.find(arguments.operation());
    if (command.isEmpty()) {
      console.error("Unsupported operation: " + arguments.operation());
      return EXIT_ERROR;
    }
    try {
      LanguageVersion version = LanguageVersion.fromLabel(arguments.versionLabel());
      command
          .get()
          .execute(new CommandRequest(arguments.filePath(), arguments.configPath(), version));
      return EXIT_OK;
    } catch (NoSuchFileException e) {
      console.error("\n[ERROR] File not found: " + e.getFile());
      return EXIT_ERROR;
    } catch (Exception e) {
      console.error("\n[ERROR] " + e.getMessage());
      return EXIT_ERROR;
    }
  }
}
