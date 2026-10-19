package org.printscript.analyzer.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.regex.Pattern;

public enum NamingConvention {
  @JsonProperty("camelCase")
  CAMEL_CASE("camelCase", "^[a-z]+([A-Z][a-z0-9]+)*$"),

  @JsonProperty("snake_case")
  SNAKE_CASE("snake_case", "^[a-z]+(_[a-z0-9]+)*$");

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
