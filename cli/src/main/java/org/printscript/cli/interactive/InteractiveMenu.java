package org.printscript.cli.interactive;

import java.util.List;
import java.util.Optional;
import org.printscript.cli.args.CliArguments;
import org.printscript.cli.command.Command;
import org.printscript.cli.command.CommandRegistry;
import org.printscript.cli.io.Console;

/**
 * Arma una {@link CliArguments} preguntándole al usuario por consola. Las opciones salen del {@link
 * CommandRegistry}, así que un comando nuevo aparece en el menú sin tocar esta clase.
 */
public class InteractiveMenu {

  private final Console console;
  private final CommandRegistry registry;

  public InteractiveMenu(Console console, CommandRegistry registry) {
    this.console = console;
    this.registry = registry;
  }

  /** Devuelve vacío si el usuario sale o elige una opción inválida. */
  public Optional<CliArguments> ask() {
    List<Command> commands = registry.all();
    printMenu(commands);

    String option = readTrimmed();
    if ("0".equals(option)) {
      console.println("Saliendo...");
      return Optional.empty();
    }
    Optional<Command> selected = select(commands, option);
    if (selected.isEmpty()) {
      console.println("Opcion invalida. Saliendo...");
      return Optional.empty();
    }
    Command command = selected.get();

    console.print("Ingrese la ruta del script (ej: script.ps): ");
    String filePath = readTrimmed();

    Optional<String> configPath = Optional.empty();
    if (command.acceptsConfig()) {
      console.print("Ingrese la ruta del JSON de configuracion (opcional, Enter para omitir): ");
      configPath = Optional.of(readTrimmed()).filter(s -> !s.isEmpty());
    }

    console.print("Ingrese la version del lenguaje (1.0 / 1.1) [default 1.0]: ");
    String version = readTrimmed();

    return Optional.of(
        new CliArguments(
            command.name(),
            filePath,
            configPath,
            version.isEmpty() ? CliArguments.DEFAULT_VERSION : version));
  }

  private void printMenu(List<Command> commands) {
    console.println("==========================================");
    console.println("      Bienvenido a PrintScript CLI        ");
    console.println("==========================================");
    console.println("Por favor, seleccione un modo de operacion:");
    for (int i = 0; i < commands.size(); i++) {
      console.println(" " + (i + 1) + ". " + capitalize(commands.get(i).name()));
    }
    console.println(" 0. Salir");
    console.print("\nIngrese el numero de la opcion: ");
  }

  private static Optional<Command> select(List<Command> commands, String option) {
    try {
      int index = Integer.parseInt(option) - 1;
      if (index >= 0 && index < commands.size()) {
        return Optional.of(commands.get(index));
      }
    } catch (NumberFormatException ignored) {
      // opción no numérica: se trata como inválida
    }
    return Optional.empty();
  }

  private String readTrimmed() {
    String line = console.readLine();
    return line == null ? "" : line.trim();
  }

  private static String capitalize(String s) {
    return Character.toUpperCase(s.charAt(0)) + s.substring(1);
  }
}
