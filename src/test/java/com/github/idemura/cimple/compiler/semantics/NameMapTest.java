package com.github.idemura.cimple.compiler.semantics;

import static com.github.idemura.cimple.compiler.ast.AstUtils.*;
import static org.junit.jupiter.api.Assertions.*;

import com.github.idemura.cimple.compiler.QualifiedName;
import org.junit.jupiter.api.Test;

class NameMapTest {
  @Test
  void testAddLocalNoCollision() {
    var nameMap = new NameMap();
    var local = localVariable("x");

    assertNull(nameMap.addLocal(local));
    assertSame(local, nameMap.lookupEntity("x"));
  }

  @Test
  void testAddLocalDuplicateLocal() {
    var nameMap = new NameMap();
    var first = localVariable("x");
    var second = localVariable("x");

    assertNull(nameMap.addLocal(first));
    assertSame(first, nameMap.addLocal(second));
    assertSame(first, nameMap.lookupEntity("x"));
  }

  @Test
  void testAddLocalDuplicateParameter() {
    var nameMap = new NameMap();
    var parameter = parameter("x");
    var local = localVariable("x");

    assertNull(nameMap.addLocal(parameter));
    assertSame(parameter, nameMap.addLocal(local));
    assertSame(parameter, nameMap.lookupEntity("x"));
  }

  @Test
  void testAddLocalShadowsAndEndScopeRestoresGlobal() {
    var nameMap = new NameMap();
    var global = globalVariable("test", "x");
    var local = localVariable("x");

    assertNull(nameMap.addVariable(global));
    nameMap.beginScope();
    assertNull(nameMap.addLocal(local));
    assertSame(local, nameMap.lookupEntity("x"));
    nameMap.endScope();

    assertSame(global, nameMap.lookupEntity("x"));
  }

  @Test
  void testReceiverFunctionHasSeparateMap() {
    var nameMap = new NameMap();
    var function = function("Duration", "toMillis");

    assertNull(nameMap.addFunction(function));
    assertNull(nameMap.lookupEntity("toMillis"));
    assertSame(
        function,
        nameMap.lookupReceiverFunction(new QualifiedName("Duration"), "toMillis"));
  }
}
