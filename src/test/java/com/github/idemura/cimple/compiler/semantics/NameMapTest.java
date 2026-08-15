package com.github.idemura.cimple.compiler.semantics;

import static com.github.idemura.cimple.compiler.ast.AstUtils.*;
import static org.junit.jupiter.api.Assertions.*;

import com.github.idemura.cimple.compiler.Identifier;
import com.github.idemura.cimple.compiler.ast.AstVariable;
import org.junit.jupiter.api.Test;

class NameMapTest {
  private static void assertLookup(NameMap nameMap, AstVariable variable) {
    assertSame(variable, nameMap.lookupEntity(Identifier.ofEntity(variable.name().entityName())));
  }

  @Test
  void testAddLocalNoCollision() {
    var nameMap = new NameMap();
    var local = localVariable("x");
    assertNull(nameMap.addLocal(local));
    assertLookup(nameMap, local);
  }

  @Test
  void testAddLocalDuplicateLocal() {
    var nameMap = new NameMap();
    var local1 = localVariable("x");
    var local2 = localVariable("x");
    assertNull(nameMap.addLocal(local1));
    assertSame(local1, nameMap.addLocal(local2));
    assertLookup(nameMap, local1);
  }

  @Test
  void testAddLocalDuplicateParameter() {
    var nameMap = new NameMap();
    var parameter = parameter("x");
    var local = localVariable("x");
    assertNull(nameMap.addLocal(parameter));
    assertSame(parameter, nameMap.addLocal(local));
    assertLookup(nameMap, parameter);
  }

  @Test
  void testAddLocalDoesNotShadowOuterLocal() {
    var nameMap = new NameMap();
    var outer = localVariable("x");
    var inner = localVariable("x");
    nameMap.beginScope();
    assertNull(nameMap.addLocal(outer));
    nameMap.beginScope();
    assertSame(outer, nameMap.addLocal(inner));
    assertLookup(nameMap, outer);
    nameMap.endScope();
    assertLookup(nameMap, outer);
    nameMap.endScope();
    assertNull(nameMap.lookupEntity(Identifier.ofEntity("x")));
  }

  @Test
  void testAddLocalDoesNotShadowOuterParameter() {
    var nameMap = new NameMap();
    var parameter = parameter("x");
    var local = localVariable("x");
    nameMap.beginScope();
    assertNull(nameMap.addLocal(parameter));
    nameMap.beginScope();
    assertSame(parameter, nameMap.addLocal(local));
    assertLookup(nameMap, parameter);
    nameMap.endScope();
    assertLookup(nameMap, parameter);
    nameMap.endScope();
    assertNull(nameMap.lookupEntity(Identifier.ofEntity("x")));
  }

  @Test
  void testAddLocalShadowsAndEndScopeRestoresGlobal() {
    var nameMap = new NameMap();
    var global = globalVariable("test", "x");
    var local = localVariable("x");
    assertNull(nameMap.addVariable(global));
    nameMap.beginScope();
    assertNull(nameMap.addLocal(local));
    assertSame(local, nameMap.lookupEntity(Identifier.ofEntity("x")));
    nameMap.endScope();
    assertSame(global, nameMap.lookupEntity(Identifier.ofEntity("x")));
  }

  @Test
  void testMethodHasSeparateMap() {
    var nameMap = new NameMap();
    var function = function("Duration", "toMillis");
    assertNull(nameMap.addFunction(function));
    assertNull(nameMap.lookupEntity(Identifier.ofEntity("toMillis")));
    assertSame(function, nameMap.lookupMethod(Identifier.ofTypeEntity("Duration", "toMillis")));
  }
}
