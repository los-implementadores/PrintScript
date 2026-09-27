package org.printscript.cli.command;

import java.util.Optional;
import org.printscript.common.LanguageVersion;

/**
 * Datos ya resueltos que recibe un {@link Command}, independientes de si vinieron por argumentos o
 * por el menú interactivo.
 */
public record CommandRequest(
    String filePath, Optional<String> configPath, LanguageVersion version) {}
