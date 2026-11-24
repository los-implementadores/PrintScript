package org.printscript.rules;

/** Qué valor acepta una clave de la configuración, para quien tiene que armar o validar una. */
public enum RuleValue {
  /** {@code true} prende la regla; {@code false} o ausente la deja apagada. */
  FLAG,
  /** {@code true} y {@code false} configuran cosas distintas (ej. con o sin espacios). */
  BOOLEAN,
  /** Un número (una cantidad de espacios, de líneas...). */
  INTEGER,
  /** Un texto (un formato, un nombre...). */
  TEXT
}
