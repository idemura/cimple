package io.lang.cimple.compiler;

import static io.lang.cimple.compiler.ast.AstUtils.*;
import static org.junit.jupiter.api.Assertions.*;

import io.lang.cimple.compiler.ast.AstVariable;
import org.junit.jupiter.api.Test;

class LocalNameMapTest {
  private static void assertLookup(LocalNameMap localNameMap, AstVariable variable) {
    assertSame(
        variable, localNameMap.lookupEntity(Identifier.ofEntity(variable.name().entityName())));
  }

  @Test
  void testAddLocalNoCollision() {
    var localNameMap = new LocalNameMap();
    var local = localVariable("x");
    assertNull(localNameMap.addLocal(local));
    assertLookup(localNameMap, local);
  }

  @Test
  void testAddLocalDuplicateLocal() {
    var localNameMap = new LocalNameMap();
    var local1 = localVariable("x");
    var local2 = localVariable("x");
    assertNull(localNameMap.addLocal(local1));
    assertSame(local1, localNameMap.addLocal(local2));
    assertLookup(localNameMap, local1);
  }

  @Test
  void testAddLocalDuplicateParameter() {
    var localNameMap = new LocalNameMap();
    var parameter = parameter("x");
    var local = localVariable("x");
    assertNull(localNameMap.addLocal(parameter));
    assertSame(parameter, localNameMap.addLocal(local));
    assertLookup(localNameMap, parameter);
  }

  @Test
  void testAddLocalDoesNotShadowOuterLocal() {
    var localNameMap = new LocalNameMap();
    var outer = localVariable("x");
    var inner = localVariable("x");
    localNameMap.beginScope();
    assertNull(localNameMap.addLocal(outer));
    localNameMap.beginScope();
    assertSame(outer, localNameMap.addLocal(inner));
    assertLookup(localNameMap, outer);
    localNameMap.endScope();
    assertLookup(localNameMap, outer);
    localNameMap.endScope();
    assertNull(localNameMap.lookupEntity(Identifier.ofEntity("x")));
  }

  @Test
  void testAddLocalDoesNotShadowOuterParameter() {
    var localNameMap = new LocalNameMap();
    var parameter = parameter("x");
    var local = localVariable("x");
    localNameMap.beginScope();
    assertNull(localNameMap.addLocal(parameter));
    localNameMap.beginScope();
    assertSame(parameter, localNameMap.addLocal(local));
    assertLookup(localNameMap, parameter);
    localNameMap.endScope();
    assertLookup(localNameMap, parameter);
    localNameMap.endScope();
    assertNull(localNameMap.lookupEntity(Identifier.ofEntity("x")));
  }

  @Test
  void testAddLocalShadowsAndEndScopeRestoresGlobal() {
    var globalNameMap = new GlobalNameMap();
    var global = globalVariable("test", "x");
    var local = localVariable("x");
    assertNull(globalNameMap.addVariable(global));
    var localNameMap = new LocalNameMap();
    assertNull(localNameMap.addEntity(global));
    localNameMap.beginScope();
    assertNull(localNameMap.addLocal(local));
    assertSame(local, localNameMap.lookupEntity(Identifier.ofEntity("x")));
    localNameMap.endScope();
    assertSame(global, localNameMap.lookupEntity(Identifier.ofEntity("x")));
  }
}
