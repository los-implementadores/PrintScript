package org.printscript.analyzer.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.regex.Pattern;

/**
 * Convenciones de nombres que acepta el linter. Desde JSON se leen con {@link #fromString}, que
 * acepta variantes como "camel case", "camelCase" o "snake_case".
 */
public enum NamingConvention {
  // Empieza en minúscula; cada palabra siguiente empieza en mayúscula. Acepta dígitos (value1) y
  // siglas (myURL).
  CAMEL_CASE("camelCase", "^[a-z][a-z0-9]*([A-Z][a-z0-9]*)*$"),

  // Minúsculas y dígitos, con palabras separadas por un solo "_" (var1, my_var_2).
  SNAKE_CASE("snake_case", "^[a-z][a-z0-9]*(_[a-z0-9]+)*$");

  private final String displayName;
  private final Pattern pattern;

  NamingConvention(String displayName, String regex) {
    this.displayName = displayName;
    this.pattern = Pattern.compile(regex);
  }

  /** Nombre con el que se muestra la convención en los mensajes (ej. {@code camelCase}). */
  public String displayName() {
    return displayName;
  }

  /** Indica si {@code name} cumple la convención. */
  public boolean matches(String name) {
    return pattern.matcher(name).matches();
  }

  @JsonCreator
  public static NamingConvention fromString(String value) {
    if (value == null) {
      return null;
    }
    String normalized = value.trim().toLowerCase().replace(" ", "_").replace("-", "_");
    if ("camelcase".equals(normalized) || "camel_case".equals(normalized)) {
      return CAMEL_CASE;
    }
    if ("snake_case".equals(normalized)) {
      return SNAKE_CASE;
    }
    throw new IllegalArgumentException("Unknown naming convention: " + value);
  }
}
