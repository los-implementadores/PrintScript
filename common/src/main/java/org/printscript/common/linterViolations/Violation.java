package org.printscript.common.linterViolations;

import org.printscript.common.Position;

public class Violation {
  private final String message;
  private final Severity severity;
  private final Position position;

  public Violation(String message, Severity severity, Position position) {
    this.message = message;
    this.severity = severity;
    this.position = position;
  }

  public String getMessage() {
    return message;
  }

  public Severity getSeverity() {
    return severity;
  }

  public Position getPosition() {
    return position;
  }

  @Override
  public String toString() {
    return "[" + severity.name() + "] " + message + " at " + position.toString();
  }
}
