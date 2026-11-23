package org.printscript.analyzer.rule;

import java.util.List;
import org.printscript.analyzer.violation.Violation;
import org.printscript.ast.Statement;

/**
 * Una regla que necesita ver el programa entero antes de reportar, como la de variables sin usar.
 *
 * <p>La regla es un valor que no cambia, como un {@code String}. Recién creada no sabe nada del
 * programa. El linter llama a {@link #onExit} al salir de cada sentencia, y la regla no se
 * modifica: devuelve otra que sabe esa sentencia más. Al terminar, el linter le pide a la última
 * {@link #onFinish}. Como nunca cambia, la misma regla sirve para cualquier cantidad de programas.
 */
public interface ProgramRule {

  /**
   * La regla después de salir de {@code statement}, ya recorrida entera.
   *
   * @return otra regla, que sabe lo mismo que esta más {@code statement}
   */
  ProgramRule onExit(Statement statement);

  /** Lo que está mal en el programa, al terminar de recorrerlo. */
  List<Violation> onFinish();
}
