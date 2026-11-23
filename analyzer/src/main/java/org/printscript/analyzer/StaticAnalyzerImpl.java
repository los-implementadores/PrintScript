package org.printscript.analyzer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.printscript.analyzer.rule.LintRules;
import org.printscript.analyzer.rule.NodeRule;
import org.printscript.analyzer.rule.ProgramRule;
import org.printscript.analyzer.violation.Violation;
import org.printscript.ast.Node;
import org.printscript.ast.Statement;
import org.printscript.rules.RuleConfig;

/**
 * Recorre cada sentencia con {@link Node#children()}: al entrar a cada nodo llama a las reglas de
 * su tipo, y al salir de cada sentencia, a las reglas de programa. No conoce ninguna regla ni
 * ningún tipo de nodo en particular: ni una regla nueva ni un nodo nuevo cambian esta clase.
 *
 * <p>No tiene estado: sus campos se arman en el constructor y no cambian más. Lo que se sabe de un
 * programa vive en las variables locales de {@link #analyze} y en las reglas de programa que van
 * devolviendo, que tampoco cambian. Así un analyzer sirve para cualquier cantidad de programas.
 */
public final class StaticAnalyzerImpl implements StaticAnalyzer {

  private static final Comparator<Violation> BY_POSITION =
      Comparator.comparingInt((Violation violation) -> violation.position().getStartLine())
          .thenComparingInt(violation -> violation.position().getStartColumn());

  /** Las reglas de nodo, indexadas por el tipo de nodo que revisan. */
  private final Map<Class<? extends Node>, List<NodeRule<?>>> nodeRulesByType;

  private final List<ProgramRule> programRules;

  /**
   * Usa las reglas que la configuración deja activas.
   *
   * @throws IllegalArgumentException si una clave tiene un valor que su regla no acepta
   */
  public StaticAnalyzerImpl(RuleConfig config) {
    this(LintRules.from(config));
  }

  /** Usa estas reglas, incluidas reglas propias que no están en la configuración. */
  public StaticAnalyzerImpl(LintRules rules) {
    this.nodeRulesByType = indexByNodeType(rules.nodeRules());
    this.programRules = rules.programRules();
  }

  @Override
  public List<Violation> analyze(Iterator<Statement> program) {
    // estado interno, no la vuelve impura
    List<Violation> violations = new ArrayList<>();
    List<ProgramRule> programRulesSoFar = programRules;
    while (program.hasNext()) {
      Statement statement = program.next();
      violations.addAll(violationsIn(statement));
      programRulesSoFar = rulesAfter(statement, programRulesSoFar);
    }
    for (ProgramRule rule : programRulesSoFar) {
      violations.addAll(rule.onFinish());
    }
    violations.sort(BY_POSITION);
    return List.copyOf(violations);
  }

  /**
   * Lo que encuentran las reglas de nodo al entrar a {@code node} y a todo lo que tiene adentro.
   */
  private List<Violation> violationsIn(Node node) {
    List<Violation> violations = new ArrayList<>();
    for (NodeRule<?> rule : nodeRulesByType.getOrDefault(node.getClass(), List.of())) {
      violations.addAll(violationsFrom(rule, node));
    }
    for (Node child : node.children()) {
      violations.addAll(violationsIn(child));
    }
    return violations;
  }

  /**
   * Lo que encuentra {@code rule} en {@code node}. Es un método aparte por los genéricos: en el
   * índice las reglas son {@code NodeRule<?>}, y acá el tipo tiene nombre ({@code N}), así que se
   * puede convertir el nodo al tipo que pide la regla. El índice garantiza que es ese.
   */
  private static <N extends Node> List<Violation> violationsFrom(NodeRule<N> rule, Node node) {
    return rule.onEnter(rule.nodeType().cast(node));
  }

  /**
   * Las reglas de programa después de salir de {@code statement}: cada una devuelve otra, que sabe
   * esa sentencia más.
   */
  private static List<ProgramRule> rulesAfter(Statement statement, List<ProgramRule> before) {
    List<ProgramRule> after = new ArrayList<>();
    for (ProgramRule rule : before) {
      after.add(rule.onExit(statement));
    }
    return after;
  }

  /** Agrupa las reglas de nodo por el tipo de nodo que revisan. */
  private static Map<Class<? extends Node>, List<NodeRule<?>>> indexByNodeType(
      List<NodeRule<?>> rules) {
    Map<Class<? extends Node>, List<NodeRule<?>>> index = new HashMap<>();
    for (NodeRule<?> rule : rules) {
      index.computeIfAbsent(rule.nodeType(), type -> new ArrayList<>()).add(rule);
    }
    index.replaceAll((type, rulesOfType) -> List.copyOf(rulesOfType));
    return Map.copyOf(index);
  }
}
