package org.printscript.rules;

/**
 * El valor con el que la configuración activó una regla: lo que recibe la fábrica de la regla en el
 * {@link RuleRegistry}.
 */
public final class RuleSetting {

  private final String key;
  private final Object value;
  private final RuleConfig config;

  RuleSetting(String key, Object value, RuleConfig config) {
    this.key = key;
    this.value = value;
    this.config = config;
  }

  /** La clave con la que vino el valor (una regla puede tener varias). */
  public String key() {
    return key;
  }

  /** {@code true} si el valor es {@code null} en el JSON. */
  public boolean isNull() {
    return value == null;
  }

  /**
   * El valor como booleano.
   *
   * @throws IllegalArgumentException si no es {@code true} ni {@code false}
   */
  public boolean asBoolean() {
    if (value instanceof Boolean flag) {
      return flag;
    }
    throw invalid("true o false");
  }

  /**
   * El valor como entero.
   *
   * @throws IllegalArgumentException si no es un número entero
   */
  public int asInt() {
    if (value instanceof Integer number) {
      return number;
    }
    throw invalid("un número entero");
  }

  /**
   * El valor como texto.
   *
   * @throws IllegalArgumentException si no es un texto
   */
  public String asText() {
    if (value instanceof String text) {
      return text;
    }
    throw invalid("un texto");
  }

  /** Toda la configuración, para las reglas que leen otras claves además de la suya. */
  public RuleConfig config() {
    return config;
  }

  private IllegalArgumentException invalid(String expected) {
    return new IllegalArgumentException(
        "La regla '" + key + "' espera " + expected + ", pero recibió " + value);
  }
}
