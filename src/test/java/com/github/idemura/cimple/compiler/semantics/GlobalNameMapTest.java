package com.github.idemura.cimple.compiler.semantics;

import static com.github.idemura.cimple.compiler.ast.AstUtils.*;
import static org.junit.jupiter.api.Assertions.*;

import com.github.idemura.cimple.compiler.Identifier;
import org.junit.jupiter.api.Test;

class GlobalNameMapTest {
  @Test
  void testMethodHasSeparateMap() {
    var globalNameMap = new GlobalNameMap();
    var function = function("Duration", "toMillis");
    assertNull(globalNameMap.addFunction(function));
    assertNull(globalNameMap.lookupEntity(Identifier.ofEntity("toMillis")));
    assertSame(function, globalNameMap.lookupEntity(Identifier.ofMethod("Duration", "toMillis")));
  }

  @Test
  void testPopulateModuleShortNames() {
    var globalNameMap = new GlobalNameMap();
    var type1 = newRecordType("m1", "Duration");
    var type2 = newRecordType("m2", "Duration");
    var var1 = globalVariable("m1", "x");
    var var2 = globalVariable("m2", "x");

    assertNull(globalNameMap.addType(type1));
    assertNull(globalNameMap.addType(type2));
    assertNull(globalNameMap.addVariable(var1));
    assertNull(globalNameMap.addVariable(var2));

    assertNull(globalNameMap.lookupType(Identifier.ofType("Duration")));
    assertNull(globalNameMap.lookupEntity(Identifier.ofEntity("x")));
    assertSame(type1, globalNameMap.lookupType(Identifier.ofType("Duration").withModule("m1")));
    assertSame(type2, globalNameMap.lookupType(Identifier.ofType("Duration").withModule("m2")));
    assertSame(var1, globalNameMap.lookupEntity(Identifier.ofEntity("x").withModule("m1")));
    assertSame(var2, globalNameMap.lookupEntity(Identifier.ofEntity("x").withModule("m2")));

    var localNameMap = globalNameMap.populateModuleShortNames("m1");
    assertSame(type1, localNameMap.lookupType(Identifier.ofType("Duration")));
    assertSame(var1, localNameMap.lookupEntity(Identifier.ofEntity("x")));
    assertNull(localNameMap.lookupType(Identifier.ofType("Duration").withModule("m2")));
    assertNull(localNameMap.lookupEntity(Identifier.ofEntity("x").withModule("m2")));
  }
}
