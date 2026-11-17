package org.printscript.formatter;

import java.io.StringWriter;
import java.util.Iterator;
import org.printscript.ast.Statement;
import org.printscript.lexer.LexerFactory;
import org.printscript.parser.GrammarFeature;
import org.printscript.parser.ParserGrammar;
import org.printscript.parser.ParserImpl;
import org.printscript.source.SourceText;

/** Formatea código de prueba con el lexer y el parser reales, como lo hace el runner. */
public final class Formatting {

  private Formatting() {}

  /** Las sentencias de {@code code}, con la gramática completa. */
  public static Iterator<Statement> parse(SourceText code) {
    return new ParserImpl(
        LexerFactory.create(code.reader()), ParserGrammar.of(GrammarFeature.values()));
  }

  /** {@code code} formateado con {@code formatter}. */
  public static String format(Formatter formatter, String code) {
    SourceText source = SourceText.recording(new java.io.StringReader(code));
    StringWriter target = new StringWriter();
    formatter.format(parse(source), source, target);
    return target.toString();
  }
}
