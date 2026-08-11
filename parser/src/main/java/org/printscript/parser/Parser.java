package org.printscript.parser;

import org.printscript.common.ast.Statement;

import java.util.Iterator;

/**
 * Analizador sintáctico de PrintScript.
 *
 * <p>Extiende {@link Iterator}{@code <Statement>} en vez de exponer un método
 * {@code parse() → Program}: así el parser produce sentencias de a una,
 * en el mismo momento en que el módulo consumidor las necesita, sin cargar
 * el árbol completo en memoria.
 *
 * <h2>Uso típico</h2>
 *
 * <p><strong>Lazy (bajo consumo de memoria) — para interpreter y formatter:</strong>
 * <pre>
 *   Lexer lexer = new LexerImpl(new StringReader(source));
 *   Parser parser = new ParserImpl(lexer);
 *   Program program = new LazyProgram(parser, startPos);
 *
 *   Iterator&lt;Statement&gt; it = program.stream();
 *   while (it.hasNext()) {
 *       it.next().accept(interpreterVisitor);
 *   }
 * </pre>
 *
 * <p><strong>Eager (árbol completo) — para analyzer:</strong>
 * <pre>
 *   Lexer lexer = new LexerImpl(new StringReader(source));
 *   Parser parser = new ParserImpl(lexer);
 *   Program program = new LazyProgram(parser, startPos);
 *
 *   List&lt;Statement&gt; all = program.toList(); // drena el parser de una
 *   analyzerVisitor.analyze(all);
 * </pre>
 *
 * <p>Si el código fuente contiene un error sintáctico, {@link #next()} lanza
 * una {@link ParseException} con la posición exacta del token inesperado
 * (fail-fast: se detiene en el primer error).
 */
public interface Parser extends Iterator<Statement> {
}
