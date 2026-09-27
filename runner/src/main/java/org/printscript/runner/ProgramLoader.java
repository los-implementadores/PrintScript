package org.printscript.runner;

import java.io.Reader;
import org.printscript.common.LanguageVersion;
import org.printscript.common.Position;
import org.printscript.common.ast.LazyProgram;
import org.printscript.common.ast.Program;
import org.printscript.parser.ParserImpl;

/** Convierte código fuente en un {@link Program} lazy (lexer + parser bajo demanda). */
public final class ProgramLoader {

  private static final Position START = new Position(1, 1, 1, 1);

  private ProgramLoader() {}

  public static Program load(Reader source, LanguageVersion version) {
    return new LazyProgram(new ParserImpl(LexerFactory.create(source), version), START);
  }
}
