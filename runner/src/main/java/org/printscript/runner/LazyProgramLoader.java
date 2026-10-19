package org.printscript.runner;

import java.io.Reader;
import org.printscript.common.LanguageVersion;
import org.printscript.common.Position;
import org.printscript.common.ast.LazyProgram;
import org.printscript.common.ast.Program;
import org.printscript.parser.ParserImpl;

/** Arma lexer y parser de PrintScript y devuelve un programa que se parsea a medida que se lee. */
final class LazyProgramLoader implements ProgramLoader {

  private static final Position START = new Position(1, 1, 1, 1);

  @Override
  public Program load(Reader source, LanguageVersion version) {
    return new LazyProgram(new ParserImpl(LexerFactory.create(source), version), START);
  }
}
