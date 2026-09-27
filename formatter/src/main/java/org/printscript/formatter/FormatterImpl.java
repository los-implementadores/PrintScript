package org.printscript.formatter;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Iterator;
import org.printscript.common.ast.Program;

/**
 * Implementación de {@link Formatter} que soporta tanto formateo desde AST (con {@link
 * FormatterVisitor}) como formateo en streaming bajo demanda (con {@link StreamingFormatter}).
 */
public class FormatterImpl implements Formatter {

  private final FormattingRules rules;

  public FormatterImpl(FormattingRules rules) {
    this.rules = rules;
  }

  @Override
  public String format(Program program) {
    FormatterVisitor visitor = new FormatterVisitor(rules);
    return program.accept(visitor);
  }

  @Override
  public void format(Reader reader, Writer writer) {
    try {
      new StreamingFormatter(reader, rules).writeTo(writer);
    } catch (IOException e) {
      throw new UncheckedIOException("Error al escribir código formateado", e);
    }
  }

  @Override
  public String format(String source) {
    StringWriter writer = new StringWriter();
    format(new StringReader(source), writer);
    return writer.toString();
  }

  @Override
  public Iterator<String> formatIterator(Reader reader) {
    return new StreamingFormatter(reader, rules);
  }
}
