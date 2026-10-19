package org.printscript.common.ast;

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.printscript.common.Position;

/**
 * Implementación de {@link Program} que drena el parser a medida que se piden las sentencias.
 *
 * <p>Envuelve un {@code Iterator<Statement>} (típicamente un {@code ParserImpl}) y no guarda
 * ninguna sentencia: el lexer, el parser y el consumidor trabajan en pipeline y en cada momento
 * sólo existe una sentencia en memoria.
 *
 * <p>Por eso admite un único recorrido: el parser ya consumió la entrada. Un segundo llamado a
 * {@link #stream()} falla en vez de devolver silenciosamente sólo lo que quedaba.
 *
 * <pre>
 *   Program program = new LazyProgram(parser, pos);
 *   Iterator&lt;Statement&gt; it = program.stream();
 *   while (it.hasNext()) {
 *       it.next().accept(interpreterVisitor);
 *   }
 * </pre>
 */
public final class LazyProgram implements Program {

  private final Iterator<Statement> source;
  private final Position position;
  private boolean consumed;

  /**
   * @param source iterator que produce las sentencias (tipicamente un ParserImpl)
   * @param position posición que abarca el programa completo en el código fuente
   */
  public LazyProgram(Iterator<Statement> source, Position position) {
    this.source = source;
    this.position = position;
  }

  /**
   * Devuelve un iterator que drena el parser de a una sentencia.
   *
   * @throws IllegalStateException si el programa ya se recorrió
   */
  @Override
  public Iterator<Statement> stream() {
    if (consumed) {
      throw new IllegalStateException(
          "LazyProgram admite un solo recorrido: el parser ya consumió la entrada.");
    }
    consumed = true;
    return new DrainIterator();
  }

  @Override
  public Position getPosition() {
    return position;
  }

  @Override
  public <T> T accept(ASTVisitor<T> visitor) {
    return visitor.visitProgram(this);
  }

  /** Delega en {@code source} respetando el contrato de {@link Iterator#next()}. */
  private class DrainIterator implements Iterator<Statement> {

    @Override
    public boolean hasNext() {
      return source.hasNext();
    }

    @Override
    public Statement next() {
      if (!source.hasNext()) {
        throw new NoSuchElementException();
      }
      return source.next();
    }
  }
}
