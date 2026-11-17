package org.printscript.source;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * El código fuente tal como se escribió, para quien lo tiene que reproducir (el formatter).
 *
 * <p>Se lee en orden con un cursor, como el {@code last_pos} de rustfmt (ver ADR-029): {@link
 * #whitespace()} devuelve los espacios que hay en el cursor y {@link #expect} avanza sobre un texto
 * que ya se conoce (una palabra reservada, un símbolo, un literal). El AST dice qué viene; este
 * texto dice cómo estaba escrito.
 *
 * <p>Con {@link #recording(Reader)} el texto se guarda a medida que el lexer lee de {@link
 * #reader()}, y lo que el cursor ya pasó se libera: en memoria queda poco más que la sentencia que
 * se está formateando.
 */
public final class SourceText {

  /** Cuánto texto ya leído se acumula antes de liberarlo. */
  private static final int RELEASE_THRESHOLD = 8192;

  private final StringBuilder text = new StringBuilder();
  private final Reader reader;
  private int cursor;

  /** El texto ya está entero; el lexer lo lee de un {@link StringReader} aparte. */
  private SourceText(String known) {
    text.append(known);
    reader = new StringReader(known);
  }

  /** El texto se va guardando a medida que el lexer lee de {@link #reader}. */
  private SourceText(Reader source) {
    reader = new Recorder(source);
  }

  /** Un texto que ya se conoce entero. {@link #reader()} lo lee desde el principio. */
  public static SourceText of(String text) {
    return new SourceText(text);
  }

  /** Un texto que se guarda a medida que alguien lee {@link #reader()}. */
  public static SourceText recording(Reader source) {
    return new SourceText(source);
  }

  /** El texto que tiene que leer el lexer. */
  public Reader reader() {
    return reader;
  }

  /** {@code true} para los caracteres que separan tokens: espacio, tab y saltos de línea. */
  public static boolean isWhitespace(char c) {
    return c == ' ' || c == '\t' || c == '\r' || c == '\n';
  }

  /** Avanza sobre los espacios que hay en el cursor y los devuelve; vacío si no hay ninguno. */
  public String whitespace() {
    int start = cursor;
    while (cursor < text.length() && isWhitespace(text.charAt(cursor))) {
      cursor++;
    }
    String whitespace = text.substring(start, cursor);
    release();
    return whitespace;
  }

  /**
   * Avanza sobre {@code expected}, que tiene que ser lo que sigue en el texto.
   *
   * @throws IllegalStateException si el texto no sigue con {@code expected}: quien lo recorre no
   *     coincide con el código original
   */
  public void expect(String expected) {
    int end = cursor + expected.length();
    if (end > text.length() || !text.substring(cursor, end).equals(expected)) {
      throw new IllegalStateException(
          "El código original no sigue con '" + expected + "' sino con '" + upcoming() + "'");
    }
    cursor = end;
    release();
  }

  /** {@code true} si el cursor llegó al final de lo que se leyó. */
  public boolean atEnd() {
    return cursor == text.length();
  }

  private String upcoming() {
    return text.substring(cursor, Math.min(text.length(), cursor + 20));
  }

  private void release() {
    if (cursor >= RELEASE_THRESHOLD) {
      text.delete(0, cursor);
      cursor = 0;
    }
  }

  /** Un {@link Reader} que guarda en el texto todo lo que se lee de él. */
  private final class Recorder extends FilterReader {

    Recorder(Reader source) {
      super(source);
    }

    @Override
    public int read() throws IOException {
      int c = super.read();
      if (c != -1) {
        text.append((char) c);
      }
      return c;
    }

    @Override
    public int read(char[] buffer, int offset, int length) throws IOException {
      int read = super.read(buffer, offset, length);
      if (read > 0) {
        text.append(buffer, offset, read);
      }
      return read;
    }
  }
}
