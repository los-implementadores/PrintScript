package org.printscript.formatter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.List;
import org.printscript.ast.Node;
import org.printscript.source.SourceText;

/**
 * Lo que escribe {@link FormatterVisitor}: las partes que ya conoce y las brechas entre ellas.
 * Lleva el cursor del código original en paralelo (el {@code last_pos} de rustfmt), la profundidad
 * de bloques y la indentación de la línea actual.
 */
final class FormattedOutput {

  private final SourceText source;
  private final List<FormattingRule> rules;
  private final Writer target;
  private int depth;
  private String lineIndent = "";

  FormattedOutput(SourceText source, List<FormattingRule> rules, Writer target) {
    this.source = source;
    this.rules = rules;
    this.target = target;
  }

  /** Escribe una parte que ya se conoce, que tiene que ser lo que sigue en el código original. */
  void text(String text) {
    source.expect(text);
    write(text);
  }

  /** Arma la brecha con el espacio original, se la pasa a las reglas y escribe lo que quedó. */
  void gap(GapKind kind, Node node, Node previous) {
    Gap gap = new Gap(kind, node, previous, source.whitespace(), new Shape(depth, lineIndent));
    for (FormattingRule rule : rules) {
      gap = gap.withWhitespace(rule.apply(gap));
    }
    write(gap.whitespace());
    lineIndent = indentAfter(gap);
  }

  /** Lo que sigue está un bloque más adentro. */
  void enterBlock() {
    depth++;
  }

  /** Lo que sigue está un bloque más afuera. */
  void exitBlock() {
    depth--;
  }

  /**
   * Comprueba que se escribió todo el código original.
   *
   * @throws IllegalStateException si quedó código sin escribir: el AST no tenía todo el programa
   */
  void finish() {
    if (!source.atEnd()) {
      throw new IllegalStateException("Quedó código original después de la última sentencia");
    }
  }

  private void write(String text) {
    try {
      target.write(text);
    } catch (IOException e) {
      throw new UncheckedIOException("Error al escribir código formateado", e);
    }
  }

  /** La indentación de la línea en la que quedó lo que sigue a {@code gap}. */
  private String indentAfter(Gap gap) {
    String written = gap.whitespace();
    int lastBreak = written.lastIndexOf('\n');
    if (lastBreak >= 0) {
      return written.substring(lastBreak + 1);
    }
    return gap.atStart() ? written : lineIndent;
  }
}
