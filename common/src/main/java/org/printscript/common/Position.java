package org.printscript.common;

import java.util.Objects;

/** Ubicación de un token o de un error dentro del archivo fuente. Fila y columna arrancan en 1. */
public final class Position {

  private final int startLine;
  private final int startColumn;
  private final int endLine;
  private final int endColumn;

  public Position(int startLine, int startColumn, int endLine, int endColumn) {
    this.startLine = startLine;
    this.startColumn = startColumn;
    this.endLine = endLine;
    this.endColumn = endColumn;
  }

  public int getStartLine() {
    return startLine;
  }

  public int getStartColumn() {
    return startColumn;
  }

  public int getEndLine() {
    return endLine;
  }

  public int getEndColumn() {
    return endColumn;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Position)) return false;
    Position position = (Position) o;
    return startLine == position.startLine
        && startColumn == position.startColumn
        && endLine == position.endLine
        && endColumn == position.endColumn;
  }

  @Override
  public int hashCode() {
    return Objects.hash(startLine, startColumn, endLine, endColumn);
  }

  @Override
  public String toString() {
    return startLine + ":" + startColumn + "-" + endLine + ":" + endColumn;
  }
}
