package org.printscript.cli.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/** {@link Console} respaldada por los streams estándar del proceso. */
public class SystemConsole implements Console {

  private final PrintStream out;
  private final PrintStream err;
  private final BufferedReader in;

  public SystemConsole() {
    this(System.out, System.err, System.in);
  }

  public SystemConsole(PrintStream out, PrintStream err, InputStream in) {
    this.out = out;
    this.err = err;
    this.in = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
  }

  @Override
  public void println(String line) {
    out.println(line);
  }

  @Override
  public void print(String text) {
    out.print(text);
    out.flush();
  }

  @Override
  public void error(String line) {
    err.println(line);
  }

  @Override
  public String readLine() {
    try {
      return in.readLine();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
