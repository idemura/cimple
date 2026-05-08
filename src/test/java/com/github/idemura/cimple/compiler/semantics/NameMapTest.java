package com.github.idemura.cimple.compiler.semantics;

import static org.junit.jupiter.api.Assertions.*;

import com.github.idemura.cimple.compiler.QualifiedName;
import com.github.idemura.cimple.compiler.ast.AstVariable;
import org.junit.jupiter.api.Test;

class NameMapTest {
  private static AstVariable makeGlobal(String name) {
    var variable = new AstVariable();
    variable.setName(new QualifiedName("test", name));
    return variable;
  }

  private static AstVariable makeLocal(String name) {
    return makeLocal(name, AstVariable.LOCAL);
  }

  private static AstVariable makeParameter(String name) {
    return makeLocal(name, AstVariable.PARAMETER);
  }

  private static AstVariable makeLocal(String name, long flag) {
    var variable = new AstVariable();
    variable.setName(new QualifiedName(name));
    variable.setBit(flag);
    return variable;
  }

  @Test
  void testAddLocalNoCollision() {
    var nameMap = new NameMap();
    var local = makeLocal("x");

    assertNull(nameMap.addLocal(local));
    assertSame(local, nameMap.lookupEntity("x"));
  }

  @Test
  void testAddLocalDuplicateLocal() {
    var nameMap = new NameMap();
    var first = makeLocal("x");
    var second = makeLocal("x");

    assertNull(nameMap.addLocal(first));
    assertSame(first, nameMap.addLocal(second));
    assertSame(first, nameMap.lookupEntity("x"));
  }

  @Test
  void testAddLocalDuplicateParameter() {
    var nameMap = new NameMap();
    var parameter = makeParameter("x");
    var local = makeLocal("x");

    assertNull(nameMap.addLocal(parameter));
    assertSame(parameter, nameMap.addLocal(local));
    assertSame(parameter, nameMap.lookupEntity("x"));
  }

  @Test
  void testAddLocalShadowsAndEndScopeRestoresGlobal() {
    var nameMap = new NameMap();
    var global = makeGlobal("x");
    var local = makeLocal("x");

    assertNull(nameMap.addVariable(global));
    nameMap.beginScope();
    assertNull(nameMap.addLocal(local));
    assertSame(local, nameMap.lookupEntity("x"));
    nameMap.endScope();

    assertSame(global, nameMap.lookupEntity("x"));
  }
}
