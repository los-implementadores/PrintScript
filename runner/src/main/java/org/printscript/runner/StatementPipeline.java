package org.printscript.runner;

import java.util.Iterator;
import java.util.List;
import org.printscript.ast.Program;
import org.printscript.ast.Statement;

/**
 * Recorre un programa de a una sentencia y le aplica cada etapa en orden, sin cargarlo entero en
 * memoria.
 *
 * <p>Es el único lugar donde los servicios iteran el programa: cada servicio sólo declara qué
 * etapas aplica (por ejemplo, análisis semántico y después ejecución).
 */
final class StatementPipeline {

  /** Algo que se hace con cada sentencia (analizarla, ejecutarla, lintearla). */
  @FunctionalInterface
  interface Stage {
    void apply(Statement statement);
  }

  private final List<Stage> stages;

  private StatementPipeline(List<Stage> stages) {
    this.stages = stages;
  }

  /** Pipeline con las etapas dadas. Sin etapas, sólo parsea cada sentencia. */
  static StatementPipeline of(Stage... stages) {
    return new StatementPipeline(List.of(stages));
  }

  /** Aplica las etapas a todas las sentencias del programa. */
  void run(Program program) {
    Iterator<Statement> statements = stream(program);
    while (statements.hasNext()) {
      statements.next();
    }
  }

  /**
   * Las sentencias del programa a medida que se piden, cada una ya pasada por las etapas. Sirve
   * para lo que necesita leer el programa entero, como el linter: las etapas corren justo antes de
   * que lea cada sentencia.
   */
  Iterator<Statement> stream(Program program) {
    Iterator<Statement> statements = program.stream();
    return new Iterator<>() {
      @Override
      public boolean hasNext() {
        return statements.hasNext();
      }

      @Override
      public Statement next() {
        Statement statement = statements.next();
        for (Stage stage : stages) {
          stage.apply(statement);
        }
        return statement;
      }
    };
  }
}
