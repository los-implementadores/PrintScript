package org.printscript.analyzer.rule;

import java.util.List;
import org.printscript.analyzer.violation.Violation;
import org.printscript.ast.Statement;

/**
 * Una regla que necesita ver el programa entero antes de reportar, como la de variables sin usar.
 *
 * <p>No guarda nada: lo que sabe del programa lo recibe y lo devuelve. Es un fold, como {@code
 * Stream.reduce}: el analyzer arranca con {@link #start()}, le pasa cada sentencia a {@link #after}
 * junto con lo que la regla sabía y se queda con lo que devuelve, y al final le pide {@link
 * #violations}. Para que eso alcance, {@code S} tiene que ser inmutable.
 *
 * @param <S> lo que la regla sabe del programa hasta una sentencia
 */
public interface ProgramRule<S> {

  /** Lo que la regla sabe antes de leer la primera sentencia. */
  S start();

  /**
   * Lo que sabe después de leer {@code statement}. No cambia {@code known}: devuelve otro valor.
   */
  S after(S known, Statement statement);

  /** Lo que está mal en el programa, cuando ya se leyó entero. */
  List<Violation> violations(S known);
}
