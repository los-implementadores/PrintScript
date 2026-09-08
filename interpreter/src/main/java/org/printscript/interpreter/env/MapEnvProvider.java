package org.printscript.interpreter.env;

import java.util.Map;

/**
 * Implementación de {@link EnvProvider} respaldada por un mapa en memoria. Diseñada para pruebas
 * unitarias y configuraciones controladas.
 */
public class MapEnvProvider implements EnvProvider {

  private final Map<String, String> envMap;

  public MapEnvProvider(Map<String, String> envMap) {
    this.envMap = Map.copyOf(envMap);
  }

  @Override
  public String getEnv(String name) {
    return envMap.get(name);
  }
}
