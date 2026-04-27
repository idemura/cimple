package com.github.idemura.cimple.compiler.semantics;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ScopeNameMapTest {
  @Test
  void testPushPop() {
    {
      var d = new ScopeNameMap<Integer>();
      d.pushScope();
      d.popScope();
    }
    {
      var d = new ScopeNameMap<Integer>();
      d.pushScope();
      d.popScope();
      d.pushScope();
      d.popScope();
    }
    {
      var d = new ScopeNameMap<Integer>();
      d.pushScope();
      d.pushScope();
      d.popScope();
      d.popScope();
    }
  }

  @Test
  void testShadow1() {
    var d = new ScopeNameMap<Integer>();
    d.pushScope();
    assertNull(d.put("x", 100));
    assertEquals(100, d.get("x"));
    assertNull(d.put("y", 200));
    assertEquals(200, d.get("y"));
    d.pushScope();
    assertNull(d.put("z", 300));
    assertEquals(300, d.get("z"));
    assertEquals(100, d.put("x", 101));
    assertEquals(100, d.get("x"));
    d.popScope();
    assertEquals(100, d.get("x"));
    assertEquals(200, d.get("y"));
    assertNull(d.get("z"));
    d.popScope();
  }

  @Test
  void testShadow2() {
    var d = new ScopeNameMap<Integer>();
    d.pushScope();
    assertNull(d.put("x", 100));
    assertEquals(100, d.get("x"));
    assertNull(d.put("y", 200));
    assertEquals(200, d.get("y"));
    d.pushScope();
    assertNull(d.put("z", 300));
    assertEquals(300, d.get("z"));
    assertEquals(100, d.put("x", 101));
    assertEquals(100, d.get("x"));
    d.pushScope();
    assertEquals(200, d.put("y", 201));
    assertEquals(200, d.get("y"));
    assertEquals(300, d.put("z", 301));
    assertEquals(300, d.get("z"));
    d.popScope();
    assertEquals(100, d.get("x"));
    assertEquals(200, d.get("y"));
    assertEquals(300, d.get("z"));
    d.popScope();
    assertEquals(100, d.get("x"));
    assertEquals(200, d.get("y"));
    assertNull(d.get("z"));
    d.popScope();
  }

  @Test
  void testConflict() {
    var d = new ScopeNameMap<Integer>();
    d.pushScope();
    assertNull(d.put("x", 100));
    assertEquals(100, d.get("x"));
    assertNull(d.put("y", 200));
    assertEquals(200, d.get("y"));
    d.pushScope();
    assertNull(d.put("z", 300));
    assertEquals(300, d.get("z"));
    assertEquals(100, d.put("x", 101));
    assertEquals(100, d.get("x"));
    assertEquals(100, d.put("x", 102));
    assertEquals(100, d.get("x"));
    assertEquals(300, d.put("z", 301));
    d.popScope();
    assertEquals(100, d.get("x"));
    assertEquals(200, d.get("y"));
    assertNull(d.get("z"));
    d.popScope();
  }

  @Test
  void testPopAll() {
    var d = new ScopeNameMap<Integer>();
    d.pushScope();
    d.put("x", 100);
    assertEquals(100, d.get("x"));
    d.pushScope();
    d.put("x", 101);
    assertEquals(100, d.get("x"));
    d.put("y", 200);
    assertEquals(200, d.get("y"));
    d.popScope();
    assertEquals(100, d.get("x"));
    assertNull(d.get("y"));
    d.popScope();
    assertNull(d.get("x"));
    assertNull(d.get("y"));
  }

  @Test
  void testGlobal() {
    var d = new ScopeNameMap<Integer>();
    assertNull(d.putGlobal("a", 100));
    assertNull(d.putGlobal("b", 200));
    assertEquals(100, d.putGlobal("a", 101));

    assertEquals(100, d.get("a"));
    assertEquals(200, d.get("b"));

    d.pushScope();

    assertNull(d.put("a", 105));
    assertEquals(105, d.get("a"));
    assertEquals(200, d.get("b"));

    d.pushScope();

    assertNull(d.put("b", 202));

    assertEquals(105, d.get("a"));
    assertEquals(202, d.get("b"));

    d.popScope();

    assertEquals(105, d.get("a"));
    assertEquals(200, d.get("b"));

    d.popScope();

    assertEquals(100, d.get("a"));
    assertEquals(200, d.get("b"));
  }
}
