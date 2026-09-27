package org.printscript.cli.command;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/** Comandos disponibles indexados por nombre (case-insensitive), en orden de registro. */
public class CommandRegistry {

  private final Map<String, Command> commands = new LinkedHashMap<>();

  public CommandRegistry(List<Command> commands) {
    for (Command command : commands) {
      this.commands.put(command.name().toLowerCase(Locale.ROOT), command);
    }
  }

  public Optional<Command> find(String name) {
    return Optional.ofNullable(commands.get(name.toLowerCase(Locale.ROOT)));
  }

  public List<Command> all() {
    return new ArrayList<>(commands.values());
  }
}
