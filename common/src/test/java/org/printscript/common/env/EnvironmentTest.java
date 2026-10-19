package org.printscript.common.env;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EnvironmentTest {

  private final Environment global = new Environment();

  @Test
  void aChildScopeSeesTheVariablesOfItsEnclosingScopes() {
    global.define("a", "number", 1.0);
    Environment inner = global.childScope().childScope();

    assertTrue(inner.isDeclared("a"));
    assertEquals(1.0, inner.get("a"));
    assertFalse(inner.isDeclaredInThisScope("a"));
  }

  @Test
  void whatAChildScopeDeclaresIsNotVisibleOutside() {
    Environment block = global.childScope();
    block.define("b", "number", 2.0);

    assertTrue(block.isDeclared("b"));
    assertFalse(global.isDeclared("b"));
  }

  @Test
  void assigningFromAChildScopeUpdatesTheScopeThatDeclaredTheVariable() {
    global.define("a", "number", 1.0);
    global.childScope().assign("a", 5.0);

    assertEquals(5.0, global.get("a"));
  }

  @Test
  void aChildScopeCanShadowAnOuterVariable() {
    global.define("x", "number", 1.0);
    Environment block = global.childScope();
    block.define("x", "string", "adentro");

    assertEquals("adentro", block.get("x"));
    assertEquals("string", block.getType("x"));
    assertEquals(1.0, global.get("x"));
  }

  @Test
  void declaringTwiceInTheSameScopeFails() {
    global.define("x", "number", 1.0);

    assertThrows(RuntimeException.class, () -> global.define("x", "number", 2.0));
  }

  @Test
  void aConstantDeclaredOutsideCannotBeReassignedFromABlock() {
    global.define("pi", "number", 3.14, true);

    assertThrows(RuntimeException.class, () -> global.childScope().assign("pi", 3.0));
  }
}
