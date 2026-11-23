package org.printscript.analyzer;

import java.util.Iterator;
import java.util.List;
import org.printscript.analyzer.violation.Violation;
import org.printscript.ast.Statement;

/**
 * El linter: revisa un programa con sus reglas y devuelve lo que encontró.
 *
 * <p>No se acuerda de nada entre un programa y otro: el mismo analyzer sirve para analizar muchos.
 */
public interface StaticAnalyzer {

  /**
   * Lee el programa de a una sentencia, sin tenerlo entero en memoria.
   *
   * @return las violaciones, ordenadas por su posición en el código
   */
  List<Violation> analyze(Iterator<Statement> program);
}
