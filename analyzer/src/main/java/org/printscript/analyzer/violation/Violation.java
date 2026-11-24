package org.printscript.analyzer.violation;

import org.printscript.source.Position;

/**
 * Algo que una regla del linter encontró en el código, con su ubicación.
 *
 * <p>Todas son advertencias: el linter solo lee código que el semántico ya aceptó, así que nada de
 * lo que encuentra impide correrlo.
 *
 * @param rule la clave de la regla que la encontró, la misma con la que se activa en la
 *     configuración (ej. {@code activeNamingConvention}): sirve para saber qué regla no se cumple
 *     sin leer el mensaje
 * @param message qué está mal y cómo arreglarlo
 * @param position dónde está, en el código original
 */
public record Violation(String rule, String message, Position position) {

  @Override
  public String toString() {
    return "[WARNING] " + message + " at " + position;
  }
}
