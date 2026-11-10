package org.printscript.formatter;

/**
 * Una regla de formato: decide el espacio entre dos tokens.
 *
 * <p>El formatter le pasa a cada regla todos los espacios del archivo, en orden y uno por vez, con
 * lo que dejaron las reglas anteriores. La regla devuelve el espacio que tiene que quedar, o {@code
 * gap.whitespace()} si ese espacio no le importa. Como ve todos los espacios en orden, puede llevar
 * estado (por ejemplo, la profundidad de llaves para indentar).
 *
 * <p>Agregar una regla es escribir una clase que implemente esta interfaz y registrarla con su
 * clave en {@link org.printscript.formatter.rule.FormattingRules}.
 */
@FunctionalInterface
public interface FormattingRule {

  /** El espacio que tiene que quedar en {@code gap}. */
  String apply(Gap gap);
}
