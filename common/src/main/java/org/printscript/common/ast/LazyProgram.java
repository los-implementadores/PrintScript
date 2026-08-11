package org.printscript.common.ast;

import org.printscript.common.Position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Implementación de {@link Program} que drena el parser on-demand.
 *
 * <p>Envuelve un {@code Iterator<Statement>} (típicamente un {@code ParserImpl})
 * y no materializa las sentencias hasta que alguien las pide. Esto permite
 * que el parser, el lexer y el módulo consumidor trabajen en pipeline:
 * en cada momento solo existe una sentencia en memoria.
 *
 * <h2>Comportamiento de cada método</h2>
 * <ul>
 *   <li>{@link #stream()} — devuelve un iterator que drena el parser de a una
 *       sentencia. No carga nada más de lo necesario. Ideal para el interpreter.</li>
 *   <li>{@link #toList()} — drena el iterator completo la primera vez y cachea
 *       el resultado en una lista inmutable. Llamadas posteriores son O(1).
 *       Ideal para el analyzer.</li>
 * </ul>
 *
 * <p><strong>Atención:</strong> {@link #stream()} y {@link #toList()} comparten
 * el mismo iterator subyacente. Si se llama {@link #stream()} primero y se consume
 * parcialmente, una llamada posterior a {@link #toList()} solo verá las sentencias
 * restantes. Usar uno u otro, no ambos sobre el mismo {@code LazyProgram}.
 *
 * <pre>
 *   Parser parser = new ParserImpl(lexer);
 *   Program program = new LazyProgram(parser, pos);
 *
 *   // opción A — lazy, bajo consumo de memoria:
 *   Iterator&lt;Statement&gt; it = program.stream();
 *   while (it.hasNext()) {
 *       it.next().accept(interpreterVisitor);
 *   }
 *
 *   // opción B — materializa todo de una:
 *   for (Statement stmt : program.toList()) {
 *       stmt.accept(analyzerVisitor);
 *   }
 * </pre>
 */
public final class LazyProgram implements Program {

    private final Iterator<Statement> source;
    private final Position position;

    /** Cache: null hasta que se llame toList() por primera vez. */
    private List<Statement> cache = null;

    /**
     * @param source   iterator que produce las sentencias (tipicamente un ParserImpl)
     * @param position posición que abarca el programa completo en el código fuente
     */
    public LazyProgram(Iterator<Statement> source, Position position) {
        this.source = source;
        this.position = position;
    }

    /**
     * Drena el iterator completo la primera vez y cachea el resultado.
     * Llamadas posteriores devuelven el cache en O(1).
     *
     * @return lista inmutable de todas las sentencias
     */
    @Override
    public List<Statement> toList() {
        if (cache == null) {
            List<Statement> result = new ArrayList<>();
            while (source.hasNext()) {
                result.add(source.next());
            }
            cache = Collections.unmodifiableList(result);
        }
        return cache;
    }

    /**
     * Devuelve un iterator lazy sobre las sentencias.
     *
     * <p>Si el cache ya fue construido (por una llamada previa a {@link #toList()}),
     * itera sobre él. Si no, drena el parser de a una sentencia sin cargar el resto.
     */
    @Override
    public Iterator<Statement> stream() {
        if (cache != null) {
            return cache.iterator();
        }
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

    /**
     * Iterator que delega en {@code source} y actualiza el cache on-the-fly.
     * Permite que stream() sea verdaderamente lazy.
     */
    private class DrainIterator implements Iterator<Statement> {

        @Override
        public boolean hasNext() {
            return source.hasNext();
        }

        @Override
        public Statement next() {
            if (!source.hasNext()) throw new NoSuchElementException();
            return source.next();
        }
    }
}
