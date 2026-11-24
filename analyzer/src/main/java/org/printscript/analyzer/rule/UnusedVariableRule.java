package org.printscript.analyzer.rule;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.printscript.analyzer.violation.Violation;
import org.printscript.ast.AssignmentStatement;
import org.printscript.ast.Block;
import org.printscript.ast.Identifier;
import org.printscript.ast.Node;
import org.printscript.ast.Statement;
import org.printscript.ast.VarDeclarationStatement;

/**
 * Reporta las variables declaradas que nunca se leen ni se reasignan, en la posición de su nombre.
 *
 * <p>Respeta los scopes de bloque: un uso cuenta para la declaración visible más cercana. Lo que la
 * regla sabe del programa es lo que hicieron con las variables las sentencias que ya pasaron
 * ({@link VariableUsage}). Ese campo no cambia: cada sentencia nueva da otra regla, con la
 * sentencia sumada con {@link VariableUsage#then}. El programa entero funciona como un bloque que
 * nunca se cierra.
 */
public final class UnusedVariableRule implements ProgramRule {

  /** La clave con la que se activa y con la que se identifican sus violaciones. */
  public static final String KEY = "activeUnusedVariables";

  /**
   * Qué hacen con las variables los tipos de nodo que importan. Cualquier otro (un literal, una
   * operación, una llamada, un {@code if}) hace lo que hacen sus hijos, uno después del otro.
   */
  private static final Map<Class<? extends Node>, Function<Node, VariableUsage>>
      USAGE_BY_NODE_TYPE =
          Map.ofEntries(
              forType(Identifier.class, UnusedVariableRule::identifierUsage),
              forType(VarDeclarationStatement.class, UnusedVariableRule::declarationUsage),
              forType(AssignmentStatement.class, UnusedVariableRule::assignmentUsage),
              forType(Block.class, UnusedVariableRule::blockUsage));

  /** Lo que hicieron con las variables las sentencias que la regla ya leyó. */
  private final VariableUsage usageSoFar;

  /** Una regla que todavía no leyó ninguna sentencia: no se declaró ninguna variable. */
  public UnusedVariableRule() {
    this(VariableUsage.NOTHING);
  }

  private UnusedVariableRule(VariableUsage usageSoFar) {
    this.usageSoFar = usageSoFar;
  }

  /** Otra regla, que además de lo que sabe esta sabe lo que hace {@code statement}. */
  @Override
  public UnusedVariableRule onExit(Statement statement) {
    return new UnusedVariableRule(usageSoFar.then(usageOf(statement)));
  }

  @Override
  public List<Violation> onFinish() {
    return usageSoFar.unused();
  }

  /** Lo que hace {@code node} con las variables. */
  private static VariableUsage usageOf(Node node) {
    return USAGE_BY_NODE_TYPE
        .getOrDefault(node.getClass(), UnusedVariableRule::usageOfChildren)
        .apply(node);
  }

  /** Lo que hacen los hijos de {@code node}, uno después del otro y en orden. */
  private static VariableUsage usageOfChildren(Node node) {
    VariableUsage usage = VariableUsage.NOTHING;
    for (Node child : node.children()) {
      usage = usage.then(usageOf(child));
    }
    return usage;
  }

  /** Un identificador lee la variable que nombra. */
  private static VariableUsage identifierUsage(Identifier identifier) {
    return VariableUsage.reading(identifier.getName());
  }

  /**
   * Primero el valor y después la variable nueva: en {@code let x: number = x + 1;} la {@code x}
   * del valor es otra, la que ya existía.
   */
  private static VariableUsage declarationUsage(VarDeclarationStatement declaration) {
    Identifier name = declaration.getName();
    DeclaredVariable variable = new DeclaredVariable(name.getName(), name.getPosition());
    return usageOfChildren(declaration).then(VariableUsage.declaring(variable));
  }

  /** Reasignar una variable cuenta como usarla. */
  private static VariableUsage assignmentUsage(AssignmentStatement assignment) {
    return VariableUsage.reading(assignment.getTarget().getName())
        .then(usageOfChildren(assignment));
  }

  /** Un bloque es sus sentencias una después de la otra; al terminar, sus variables desaparecen. */
  private static VariableUsage blockUsage(Block block) {
    return usageOfChildren(block).asClosedBlock();
  }

  /**
   * Una entrada de la tabla: el tipo de nodo y qué hace con las variables. Le pasa el nodo con su
   * tipo, que la tabla ya garantiza.
   */
  private static <N extends Node>
      Map.Entry<Class<? extends Node>, Function<Node, VariableUsage>> forType(
          Class<N> type, Function<N, VariableUsage> usage) {
    return Map.entry(type, node -> usage.apply(type.cast(node)));
  }
}
