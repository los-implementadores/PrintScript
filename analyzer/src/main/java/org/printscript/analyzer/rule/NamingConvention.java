package org.printscript.analyzer.rule;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Convenciones de nombres que acepta el linter. Desde la configuración se leen con {@link
 * #fromString}, que acepta variantes como "camel case", "camelCase" o "snake_case".
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

  /**
   * Cómo se puede escribir cada convención en la configuración, ya en minúsculas y con {@code _} en
   * vez de espacios y guiones.
   */
  private static final Map<String, NamingConvention> BY_CONFIGURED_NAME =
      Map.of("camelcase", CAMEL_CASE, "camel_case", CAMEL_CASE, "snake_case", SNAKE_CASE);

  /**
   * @throws IllegalArgumentException si no es ninguna de las convenciones conocidas
   */
  public static NamingConvention fromString(String value) {
    String normalized = value.trim().toLowerCase().replace(" ", "_").replace("-", "_");
    NamingConvention convention = BY_CONFIGURED_NAME.get(normalized);
    if (convention == null) {
      throw new IllegalArgumentException("Unknown naming convention: " + value);
    }
    return convention;
  }
}
