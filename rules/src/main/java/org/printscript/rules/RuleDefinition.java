package org.printscript.rules;

import java.util.List;

/**
 * Una regla que existe, descripta para quien arma la configuración sin conocer las clases de la
 * herramienta (una UI, un servicio que guarda las reglas de cada usuario).
 *
 * @param key la clave propia de la regla
 * @param aliases otras claves que la activan (por ejemplo, la del TCK)
 * @param value qué valor acepta la clave
 * @param parameters otras claves que la regla lee cuando está activa (por ejemplo, la convención de
 *     nombres)
 */
public record RuleDefinition(
    String key, List<String> aliases, RuleValue value, List<Parameter> parameters) {

  public RuleDefinition {
    aliases = List.copyOf(aliases);
    parameters = List.copyOf(parameters);
  }

  /**
   * Una clave que configura a la regla pero no la activa por sí sola.
   *
   * @param key la clave
   * @param value qué valor acepta
   */
  public record Parameter(String key, RuleValue value) {}
}
