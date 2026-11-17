package org.printscript.formatter;

/**
 * Una regla de formato: decide el espacio de una brecha del AST.
 *
 * <p>El formatter le pasa a cada regla todas las brechas del programa, en orden y una por vez, con
 * lo que dejaron las reglas anteriores. La regla mira qué brecha es ({@link Gap#kind()}) y de qué
 * nodo ({@link Gap#node()}), y devuelve el espacio que tiene que quedar, o {@code gap.whitespace()}
 * si esa brecha no le importa. Lo que necesita saber del programa (la profundidad de bloques, la
 * sentencia anterior) viene en la brecha: no hace falta que guarde estado ni que reconstruya la
 * gramática.
 *
 * <p>Agregar una regla es escribir una clase que implemente esta interfaz y registrarla con su
 * clave en {@link org.printscript.formatter.rule.FormattingRules}.
 */
@FunctionalInterface
public interface FormattingRule {

  /** El espacio que tiene que quedar en {@code gap}. */
  String apply(Gap gap);
}
