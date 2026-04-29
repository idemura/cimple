package com.github.idemura.cimple.compiler.semantics;

import static org.junit.jupiter.api.Assertions.*;

import com.github.idemura.cimple.compiler.ast.QualifiedName;
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
    assertNull(d.put(new QualifiedName("x"), 100));
    assertEquals(100, d.get(new QualifiedName("x")));
    assertNull(d.put(new QualifiedName("y"), 200));
    assertEquals(200, d.get(new QualifiedName("y")));
    d.pushScope();
    assertNull(d.put(new QualifiedName("z"), 300));
    assertEquals(300, d.get(new QualifiedName("z")));
    assertEquals(100, d.put(new QualifiedName("x"), 101));
    assertEquals(100, d.get(new QualifiedName("x")));
    d.popScope();
    assertEquals(100, d.get(new QualifiedName("x")));
    assertEquals(200, d.get(new QualifiedName("y")));
    assertNull(d.get(new QualifiedName("z")));
    d.popScope();
  }

  @Test
  void testShadow2() {
    var d = new ScopeNameMap<Integer>();
    d.pushScope();
    assertNull(d.put(new QualifiedName("x"), 100));
    assertEquals(100, d.get(new QualifiedName("x")));
    assertNull(d.put(new QualifiedName("y"), 200));
    assertEquals(200, d.get(new QualifiedName("y")));
    d.pushScope();
    assertNull(d.put(new QualifiedName("z"), 300));
    assertEquals(300, d.get(new QualifiedName("z")));
    assertEquals(100, d.put(new QualifiedName("x"), 101));
    assertEquals(100, d.get(new QualifiedName("x")));
    d.pushScope();
    assertEquals(200, d.put(new QualifiedName("y"), 201));
    assertEquals(200, d.get(new QualifiedName("y")));
    assertEquals(300, d.put(new QualifiedName("z"), 301));
    assertEquals(300, d.get(new QualifiedName("z")));
    d.popScope();
    assertEquals(100, d.get(new QualifiedName("x")));
    assertEquals(200, d.get(new QualifiedName("y")));
    assertEquals(300, d.get(new QualifiedName("z")));
    d.popScope();
    assertEquals(100, d.get(new QualifiedName("x")));
    assertEquals(200, d.get(new QualifiedName("y")));
    assertNull(d.get(new QualifiedName("z")));
    d.popScope();
  }

  @Test
  void testConflict() {
    var d = new ScopeNameMap<Integer>();
    d.pushScope();
    assertNull(d.put(new QualifiedName("x"), 100));
    assertEquals(100, d.get(new QualifiedName("x")));
    assertNull(d.put(new QualifiedName("y"), 200));
    assertEquals(200, d.get(new QualifiedName("y")));
    d.pushScope();
    assertNull(d.put(new QualifiedName("z"), 300));
    assertEquals(300, d.get(new QualifiedName("z")));
    assertEquals(100, d.put(new QualifiedName("x"), 101));
    assertEquals(100, d.get(new QualifiedName("x")));
    assertEquals(100, d.put(new QualifiedName("x"), 102));
    assertEquals(100, d.get(new QualifiedName("x")));
    assertEquals(300, d.put(new QualifiedName("z"), 301));
    d.popScope();
    assertEquals(100, d.get(new QualifiedName("x")));
    assertEquals(200, d.get(new QualifiedName("y")));
    assertNull(d.get(new QualifiedName("z")));
    d.popScope();
  }

  @Test
  void testPopAll() {
    var d = new ScopeNameMap<Integer>();
    d.pushScope();
    d.put(new QualifiedName("x"), 100);
    assertEquals(100, d.get(new QualifiedName("x")));
    d.pushScope();
    d.put(new QualifiedName("x"), 101);
    assertEquals(100, d.get(new QualifiedName("x")));
    d.put(new QualifiedName("y"), 200);
    assertEquals(200, d.get(new QualifiedName("y")));
    d.popScope();
    assertEquals(100, d.get(new QualifiedName("x")));
    assertNull(d.get(new QualifiedName("y")));
    d.popScope();
    assertNull(d.get(new QualifiedName("x")));
    assertNull(d.get(new QualifiedName("y")));
  }

  @Test
  void testGlobal() {
    var d = new ScopeNameMap<Integer>();
    assertNull(d.putGlobal(new QualifiedName("a"), 100));
    assertNull(d.putGlobal(new QualifiedName("b"), 200));
    assertEquals(100, d.putGlobal(new QualifiedName("a"), 101));

    assertEquals(100, d.get(new QualifiedName("a")));
    assertEquals(200, d.get(new QualifiedName("b")));

    d.pushScope();

    assertNull(d.put(new QualifiedName("a"), 105));
    assertEquals(105, d.get(new QualifiedName("a")));
    assertEquals(200, d.get(new QualifiedName("b")));

    d.pushScope();

    assertNull(d.put(new QualifiedName("b"), 202));

    assertEquals(105, d.get(new QualifiedName("a")));
    assertEquals(202, d.get(new QualifiedName("b")));

    d.popScope();

    assertEquals(105, d.get(new QualifiedName("a")));
    assertEquals(200, d.get(new QualifiedName("b")));

    d.popScope();

    assertEquals(100, d.get(new QualifiedName("a")));
    assertEquals(200, d.get(new QualifiedName("b")));
  }
}
