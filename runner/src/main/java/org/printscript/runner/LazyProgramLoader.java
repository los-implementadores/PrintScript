package org.printscript.runner;

import java.io.Reader;
import org.printscript.common.Position;
import org.printscript.common.ast.LazyProgram;
import org.printscript.common.ast.Program;
import org.printscript.lexer.LexerFactory;
import org.printscript.parser.ParserImpl;

/**
 * Arma lexer y parser con la gramática de la versión y devuelve un programa que se parsea a medida
 * que se lee.
 */
final class LazyProgramLoader implements ProgramLoader {

  private static final Position START = new Position(1, 1, 1, 1);

  @Override
  public Program load(Reader source, LanguageVersion version) {
    return new LazyProgram(new ParserImpl(LexerFactory.create(source), version.grammar()), START);
  }
}
