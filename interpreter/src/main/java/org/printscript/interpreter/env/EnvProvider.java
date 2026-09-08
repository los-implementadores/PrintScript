package org.printscript.interpreter.env;

/**
 * Abstracción inyectable para la consulta de variables de ambiente.
 *
 * <p>Permite desacoplar la ejecución de {@link System#getenv(String)}, facilitando el testing con
 * entornos simulados sin tocar variables reales del sistema operativo.
 */
@FunctionalInterface
public interface EnvProvider {

  /**
   * Retorna el valor de la variable de ambiente solicitada.
   *
   * @param name nombre de la variable de ambiente
   * @return valor de la variable, o {@code null} si no está definida
   */
  String getEnv(String name);
}
