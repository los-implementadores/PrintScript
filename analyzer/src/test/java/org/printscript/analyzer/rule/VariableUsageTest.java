package org.printscript.analyzer.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.printscript.analyzer.violation.Violation;
import org.printscript.source.Position;

/** {@link VariableUsage#then} junta pedazos de código como un reduce: asociativo y con neutro. */
class VariableUsageTest {

  private static final DeclaredVariable X = new DeclaredVariable("x", new Position(1, 5, 1, 5));
  private static final DeclaredVariable Y = new DeclaredVariable("y", new Position(2, 5, 2, 5));

  private static final List<VariableUsage> PIECES =
      List.of(
          VariableUsage.declaring(X),
          VariableUsage.reading("x"),
          VariableUsage.declaring(Y),
          VariableUsage.reading("z"),
          VariableUsage.reading("y"));

  @Test
  void nothingChangesNothing() {
    for (VariableUsage piece : PIECES) {
      assertEquals(piece, VariableUsage.NOTHING.then(piece));
      assertEquals(piece, piece.then(VariableUsage.NOTHING));
    }
  }

  @Test
  void thenIsAssociative() {
    for (VariableUsage first : PIECES) {
      for (VariableUsage second : PIECES) {
        for (VariableUsage third : PIECES) {
          assertEquals(first.then(second).then(third), first.then(second.then(third)));
        }
      }
    }
  }

  @Test
  void aReadAfterTheDeclarationIsAUseAndBeforeItIsFromOutside() {
    VariableUsage readAfter = VariableUsage.declaring(X).then(VariableUsage.reading("x"));
    VariableUsage readBefore = VariableUsage.reading("x").then(VariableUsage.declaring(X));

    assertTrue(readAfter.unused().isEmpty());
    assertEquals(Set.of(), readAfter.readsFromOutside());
    assertEquals(List.of(X.neverUsed()), readBefore.unused());
    assertEquals(Set.of("x"), readBefore.readsFromOutside());
  }

  @Test
  void aClosedBlockReportsItsUnusedVariablesAndKeepsOnlyWhatItReadFromOutside() {
    VariableUsage block =
        VariableUsage.declaring(Y).then(VariableUsage.reading("x")).asClosedBlock();
    VariableUsage outside = VariableUsage.declaring(X).then(block);

    List<Violation> unused = outside.unused();

    assertEquals(List.of(Y.neverUsed()), unused);
  }
}
