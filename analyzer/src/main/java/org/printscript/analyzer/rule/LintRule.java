package org.printscript.analyzer.rule;

import java.util.function.Consumer;
import org.printscript.analyzer.violation.Violation;
import org.printscript.common.ast.AssignmentStatement;
import org.printscript.common.ast.CallExpression;
import org.printscript.common.ast.Identifier;
import org.printscript.common.ast.VarDeclarationStatement;

/**
 * Una regla del linter.
 *
 * <p>El {@code AnalyzerVisitor} recorre el AST una sola vez y le avisa a cada regla los nodos que
 * le interesan. La regla reporta lo que encuentre llamando a {@code report}. Todos los métodos
 * tienen una implementación vacía, así cada regla sobreescribe solo los nodos que mira.
 *
 * <p>Agregar una regla es escribir una clase nueva que implemente esta interfaz y registrarla en
 * {@link LintRules}; el visitor no se toca.
 */
public interface LintRule {

  /** Se llama por cada declaración ({@code let} o {@code const}), antes de recorrer su valor. */
  default void onDeclaration(VarDeclarationStatement node, Consumer<Violation> report) {}

  /** Se llama por cada asignación, después de recorrer el valor asignado. */
  default void onAssignment(AssignmentStatement node, Consumer<Violation> report) {}

  /** Se llama por cada llamada a función, antes de recorrer sus argumentos. */
  default void onCall(CallExpression node, Consumer<Violation> report) {}

  /** Se llama cada vez que se lee una variable. */
  default void onIdentifier(Identifier node, Consumer<Violation> report) {}

  /**
   * Se llama al entrar a un bloque ({@code if} o {@code else}), antes de recorrer sus sentencias.
   * Lo que se declara adentro deja de existir en {@link #onBlockEnd}.
   */
  default void onBlockStart() {}

  /** Se llama al salir de un bloque, después de recorrer sus sentencias. */
  default void onBlockEnd() {}

  /**
   * Se llama al pedir el reporte, para las reglas que necesitan haber visto todo el programa (por
   * ejemplo, variables sin usar). Puede llamarse más de una vez, así que no debe cambiar el estado
   * de la regla.
   */
  default void onFinish(Consumer<Violation> report) {}
}
