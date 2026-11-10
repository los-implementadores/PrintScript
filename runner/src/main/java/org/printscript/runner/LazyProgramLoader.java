package org.printscript.runner;

import java.io.Reader;
import org.printscript.ast.LazyProgram;
import org.printscript.ast.Program;
import org.printscript.lexer.LexerFactory;
import org.printscript.parser.ParserImpl;

/**
 * Arma lexer y parser con la gramática de la versión y devuelve un programa que se parsea a medida
 * que se lee.
 */
final class LazyProgramLoader implements ProgramLoader {

  @Override
  public Program load(Reader source, LanguageVersion version) {
    return new LazyProgram(new ParserImpl(LexerFactory.create(source), version.grammar()));
  }
}
