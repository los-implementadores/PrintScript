package org.printscript.interpreter.env;

/**
 * Implementación de {@link EnvProvider} que consulta las variables de ambiente reales del sistema
 * operativo a través de {@link System#getenv(String)}.
 */
public class SystemEnvProvider implements EnvProvider {

  @Override
  public String getEnv(String name) {
    return System.getenv(name);
  }
}
