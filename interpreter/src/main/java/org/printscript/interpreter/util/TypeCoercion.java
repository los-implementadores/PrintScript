package org.printscript.interpreter.util;

import org.printscript.common.Position;

/**
 * Utilidad compartida para la coerción de tipos desde fuentes textuales ({@code readInput}, {@code
 * readEnv}) hacia los tipos soportados por PrintScript ({@code string}, {@code number}, {@code
 * boolean}).
 */
public final class TypeCoercion {

  private TypeCoercion() {}

  /**
   * Coerciona un valor textual al tipo objetivo especificado.
   *
   * @param raw valor textual original
   * @param targetType tipo esperado ("string", "number", "boolean")
   * @param pos posición en el código para el reporte de error
   * @param descriptor descripción de la fuente (ej. "input" o "env variable")
   * @return valor convertido a {@link String}, {@link Double} o {@link Boolean}
   */
  public static Object coerce(String raw, String targetType, Position pos, String descriptor) {
    return switch (targetType) {
      case "string" -> raw;
      case "number" -> {
        try {
          yield Double.parseDouble(raw.trim());
        } catch (NumberFormatException e) {
          throw new RuntimeException(
              "Execution Error at "
                  + pos
                  + ": Cannot coerce "
                  + descriptor
                  + " '"
                  + raw
                  + "' to number.");
        }
      }
      case "boolean" -> {
        String trimmed = raw.trim();
        if ("true".equalsIgnoreCase(trimmed)) {
          yield Boolean.TRUE;
        } else if ("false".equalsIgnoreCase(trimmed)) {
          yield Boolean.FALSE;
        } else {
          throw new RuntimeException(
              "Execution Error at "
                  + pos
                  + ": Cannot coerce "
                  + descriptor
                  + " '"
                  + raw
                  + "' to boolean.");
        }
      }
      default -> raw;
    };
  }

  /** Coerciona un valor textual con descriptor por defecto ("input"). */
  public static Object coerce(String raw, String targetType, Position pos) {
    return coerce(raw, targetType, pos, "input");
  }
}
