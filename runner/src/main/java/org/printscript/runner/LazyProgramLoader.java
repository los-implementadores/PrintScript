package org.printscript.runner;

import java.io.Reader;
import org.printscript.ast.LazyProgram;
import org.printscript.ast.Program;
import org.printscript.lexer.LexerFactory;
import org.printscript.parser.ParserImpl;
import org.printscript.source.Position;

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
