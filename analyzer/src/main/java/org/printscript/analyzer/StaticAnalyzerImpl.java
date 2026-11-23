package org.printscript.analyzer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.printscript.analyzer.rule.LintRules;
import org.printscript.analyzer.rule.NodeRule;
import org.printscript.analyzer.rule.ProgramRule;
import org.printscript.analyzer.violation.Violation;
import org.printscript.ast.Node;
import org.printscript.ast.Statement;
import org.printscript.rules.RuleConfig;

/**
 * Recorre cada sentencia con {@link Node#children()} y le pasa cada nodo a las reglas de su tipo, y
 * cada sentencia entera a las reglas de programa. No conoce ninguna regla ni ningún tipo de nodo en
 * particular: ni una regla nueva ni un nodo nuevo cambian esta clase.
 *
 * <p>No tiene estado: todo lo que se sabe de un programa vive en las variables locales de {@link
 * #analyze}, así que un analyzer sirve para cualquier cantidad de programas.
 */
public final class StaticAnalyzerImpl implements StaticAnalyzer {

  private static final Comparator<Violation> BY_POSITION =
      Comparator.comparingInt((Violation violation) -> violation.position().getStartLine())
          .thenComparingInt(violation -> violation.position().getStartColumn());

  /** Las reglas de nodo, indexadas por el tipo de nodo que revisan. */
  private final Map<Class<? extends Node>, List<NodeRule<?>>> nodeRules;

  private final List<ProgramRule<?>> programRules;

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
    this.nodeRules =
        Map.copyOf(
            rules.nodeRules().stream()
                .collect(
                    Collectors.<NodeRule<?>, Class<? extends Node>>groupingBy(NodeRule::nodeType)));
    this.programRules = rules.programRules();
  }

  @Override
  public List<Violation> analyze(Iterator<Statement> program) {
    List<Violation> found = new ArrayList<>();
    List<Progress<?>> progress = programRules.stream().<Progress<?>>map(Progress::start).toList();
    while (program.hasNext()) {
      Statement statement = program.next();
      check(statement).forEach(found::add);
      progress = progress.stream().<Progress<?>>map(rule -> rule.after(statement)).toList();
    }
    progress.forEach(rule -> found.addAll(rule.violations()));
    found.sort(BY_POSITION);
    return List.copyOf(found);
  }

  /** Lo que encuentran las reglas de nodo en {@code node} y en todo lo que tiene adentro. */
  private Stream<Violation> check(Node node) {
    Stream<Violation> here =
        nodeRules.getOrDefault(node.getClass(), List.of()).stream()
            .flatMap(rule -> check(rule, node).stream());
    return Stream.concat(here, node.children().stream().flatMap(this::check));
  }

  /** Le pasa el nodo a la regla con el tipo que ella pide: el índice ya garantiza que es ese. */
  private static <N extends Node> List<Violation> check(NodeRule<N> rule, Node node) {
    return rule.check(rule.nodeType().cast(node));
  }

  /**
   * Una regla de programa con lo que sabe hasta la última sentencia leída. Inmutable: leer otra
   * sentencia devuelve otro.
   */
  private record Progress<S>(ProgramRule<S> rule, S known) {

    static <S> Progress<S> start(ProgramRule<S> rule) {
      return new Progress<>(rule, rule.start());
    }

    Progress<S> after(Statement statement) {
      return new Progress<>(rule, rule.after(known, statement));
    }

    List<Violation> violations() {
      return rule.violations(known);
    }
  }
}
