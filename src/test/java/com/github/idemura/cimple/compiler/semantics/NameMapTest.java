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

  private static NameMap populateTestModuleShortNames(NameMap nameMap) {
    return nameMap.populateModuleShortNames("test");
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
    var moduleNameMap = populateTestModuleShortNames(nameMap);
    moduleNameMap.beginScope();
    assertNull(moduleNameMap.addLocal(local));
    assertSame(local, moduleNameMap.lookupEntity(Identifier.ofEntity("x")));
    moduleNameMap.endScope();
    assertSame(global, moduleNameMap.lookupEntity(Identifier.ofEntity("x")));
  }

  @Test
  void testMethodHasSeparateMap() {
    var nameMap = new NameMap();
    var function = function("Duration", "toMillis");
    assertNull(nameMap.addFunction(function));
    assertNull(nameMap.lookupEntity(Identifier.ofEntity("toMillis")));
    assertSame(function, nameMap.lookupMethod(Identifier.ofTypeEntity("Duration", "toMillis")));
  }

  @Test
  void testPopulateModuleShortNames() {
    var nameMap = new NameMap();
    var type1 = newRecordType("m1", "Duration");
    var type2 = newRecordType("m2", "Duration");
    var var1 = globalVariable("m1", "x");
    var var2 = globalVariable("m2", "x");

    assertNull(nameMap.addType(type1));
    assertNull(nameMap.addType(type2));
    assertNull(nameMap.addVariable(var1));
    assertNull(nameMap.addVariable(var2));

    assertNull(nameMap.lookupType(Identifier.ofType("Duration")));
    assertNull(nameMap.lookupEntity(Identifier.ofEntity("x")));
    assertSame(type1, nameMap.lookupType(Identifier.ofType("Duration").withModule("m1")));
    assertSame(type2, nameMap.lookupType(Identifier.ofType("Duration").withModule("m2")));
    assertSame(var1, nameMap.lookupEntity(Identifier.ofEntity("x").withModule("m1")));
    assertSame(var2, nameMap.lookupEntity(Identifier.ofEntity("x").withModule("m2")));

    var testNameMap = nameMap.populateModuleShortNames("m1");
    assertSame(type1, testNameMap.lookupType(Identifier.ofType("Duration")));
    assertSame(var1, testNameMap.lookupEntity(Identifier.ofEntity("x")));
    assertSame(type2, testNameMap.lookupType(Identifier.ofType("Duration").withModule("m2")));
    assertSame(var2, testNameMap.lookupEntity(Identifier.ofEntity("x").withModule("m2")));
  }
}
