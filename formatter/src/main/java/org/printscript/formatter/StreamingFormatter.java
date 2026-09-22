package org.printscript.formatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.UnaryOperator;

/**
 * Formateador en streaming que procesa código fuente bajo demanda desde un {@link Reader} con
 * memoria constante.
 */
public class StreamingFormatter implements Iterator<String> {

  private final BufferedReader reader;
  private final FormattingRules rules;
  private final Deque<String> buffer = new ArrayDeque<>();
  private int currentDepth;
  private boolean reachedEof;
  private String pendingLine;

  public StreamingFormatter(Reader input, FormattingRules rules) {
    this.reader =
        input instanceof BufferedReader ? (BufferedReader) input : new BufferedReader(input);
    this.rules = rules;
  }

  @Override
  public boolean hasNext() {
    fillBuffer();
    return !buffer.isEmpty();
  }

  @Override
  public String next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    return buffer.pollFirst();
  }

  /**
   * Escribe todo el flujo formateado al {@link Writer} destino sin almacenar el archivo completo en
   * memoria.
   */
  public void writeTo(Writer writer) throws IOException {
    boolean first = true;
    while (hasNext()) {
      if (!first) {
        writer.write('\n');
      }
      first = false;
      writer.write(next());
    }
  }

  private void fillBuffer() {
    while (buffer.isEmpty() && !reachedEof) {
      String rawLine = readNextRawLine();
      if (rawLine == null) {
        reachedEof = true;
        break;
      }
      processRawLine(rawLine);
    }
  }

  private String readNextRawLine() {
    if (pendingLine != null) {
      String line = pendingLine;
      pendingLine = null;
      return line;
    }
    try {
      return reader.readLine();
    } catch (IOException e) {
      throw new UncheckedIOException("Error leyendo código fuente para formateo", e);
    }
  }

  private void processRawLine(String rawLine) {
    List<String> splitLines = splitLineIfNeeded(rawLine);
    for (int i = 0; i < splitLines.size(); i++) {
      String line = splitLines.get(i);
      line = handleBraceSameLine(line);
      String formatted = formatLineContent(line);
      buffer.add(formatted);
      addBlankLinesAfterPrintlnIfNeeded(line);
    }
  }

  private List<String> splitLineIfNeeded(String line) {
    List<String> result = new ArrayList<>();
    if (rules.isLineBreakAfterStatement()) {
      String[] parts = line.split("(?<=;)[ \\t]*(?=[^\\r\\n\\s])");
      for (String part : parts) {
        result.addAll(splitIfBraceBelow(part));
      }
    } else {
      result.addAll(splitIfBraceBelow(line));
    }
    return result;
  }

  private List<String> splitIfBraceBelow(String line) {
    if (rules.isIfBraceBelowLine() && line.matches(".*\\bif\\s*\\([^)]*\\)[ \\t]*\\{.*")) {
      String ifPart = line.replaceFirst("[ \\t]*\\{.*", "");
      return List.of(ifPart, "{");
    }
    return List.of(line);
  }

  private String handleBraceSameLine(String line) {
    if (rules.isIfBraceSameLine() && line.matches("^[ \\t]*if\\s*\\([^)]*\\)[ \\t]*$")) {
      String next = readNextRawLine();
      if (next != null && next.trim().equals("{")) {
        return line.trim() + " {";
      }
      pendingLine = next;
    }
    return line;
  }

  private String formatLineContent(String line) {
    String current = line;
    if (rules.hasIndentSize()) {
      current = applyIndentation(current);
    }
    if (rules.isNoSpacingAroundAssign()) {
      current = applyOutsideQuotes(current, s -> s.replaceAll("[ \\t]*=[ \\t]*", "="));
    }
    if (rules.isSpaceAroundAssign()) {
      current = applyOutsideQuotes(current, s -> s.replaceAll("[ \\t]*=[ \\t]*", " = "));
    }
    if (rules.isSpaceAfterColon()) {
      current = applyOutsideQuotes(current, s -> s.replaceAll(":[ \\t]*(?=[a-zA-Z_])", ": "));
    }
    if (rules.isSpaceBeforeColon()) {
      current = applyOutsideQuotes(current, s -> s.replaceAll("(?<=[a-zA-Z0-9_])[ \\t]*:", " :"));
    }
    if (rules.isSpaceAroundOperators()) {
      current = applyOperatorSpacing(current);
    }
    if (rules.isSingleSpaceSeparation()) {
      current = applySingleSpaceSeparation(current);
    }
    return current;
  }

  private String applyIndentation(String line) {
    String trimmed = line.trim();
    if (trimmed.isEmpty()) {
      return "";
    }
    int lineDepth = currentDepth;
    if (trimmed.startsWith("}")) {
      lineDepth = Math.max(0, currentDepth - 1);
    }
    int indentSize = rules.getIndentSize();
    String indented = " ".repeat(lineDepth * indentSize) + trimmed;
    long openCount = trimmed.chars().filter(ch -> ch == '{').count();
    long closeCount = trimmed.chars().filter(ch -> ch == '}').count();
    currentDepth += (int) (openCount - closeCount);
    return indented;
  }

  private void addBlankLinesAfterPrintlnIfNeeded(String line) {
    if (rules.hasNewlineBeforePrintln() && line.contains("println")) {
      drainExistingBlankLines();
      if (pendingLine != null && !pendingLine.trim().isEmpty()) {
        for (int b = 0; b < rules.getNewlineBeforePrintln(); b++) {
          buffer.add("");
        }
      }
    }
  }

  private void drainExistingBlankLines() {
    String next = readNextRawLine();
    while (next != null && next.trim().isEmpty()) {
      next = readNextRawLine();
    }
    pendingLine = next;
  }

  private static String applyOperatorSpacing(String src) {
    return applyOutsideQuotes(
        src,
        s -> {
          String res = s.replaceAll("[ \\t]*([+*\\/])[ \\t]*", " $1 ");
          return res.replaceAll("(?<=[0-9a-zA-Z_)])[ \\t]*-[ \\t]*", " - ");
        });
  }

  private static String applySingleSpaceSeparation(String src) {
    return applyOutsideQuotes(
        src,
        s -> {
          String p = s.replaceAll("[ \\t]+", " ");
          p = p.replaceAll("[ \\t]*:[ \\t]*", " : ");
          p = p.replaceAll("[ \\t]*=[ \\t]*", " = ");
          p = p.replaceAll("(?<=[a-zA-Z0-9_])[ \\t]*\\(", " ( ");
          p = p.replaceAll("\\([ \\t]*(?=[a-zA-Z0-9_])", "( ");
          p = p.replaceAll("(?<=[a-zA-Z0-9_])[ \\t]*\\)", " )");
          return p.replaceAll("[ \\t]*;", ";");
        });
  }

  private static String applyOutsideQuotes(String src, UnaryOperator<String> transform) {
    String[] parts = src.split("\"", -1);
    for (int i = 0; i < parts.length; i += 2) {
      parts[i] = transform.apply(parts[i]);
    }
    return String.join("\"", parts);
  }
}
