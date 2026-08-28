package com.github.idemura.cimple.compiler;

import static com.github.idemura.cimple.compiler.ast.AstUtils.*;
import static org.junit.jupiter.api.Assertions.*;

import com.github.idemura.cimple.compiler.ast.AstFunction;
import com.github.idemura.cimple.compiler.ast.AstModule;
import com.github.idemura.cimple.compiler.ast.AstType;
import java.util.List;
import org.junit.jupiter.api.Test;

class GlobalNameMapTest {
  private static AstModule module(String name) {
    var module = new AstModule();
    module.name(name);
    return module;
  }

  private static AstFunction freeFunction(String moduleName, String name) {
    var function = function(name);
    function.name(function.name().withModule(moduleName));
    return function;
  }

  private static void renameType(Identifier name, int line, AstType type) {
    type.name(name);
    type.location(new Location(line, 1));
  }

  @Test
  void testMethodIsStoredAsQualifiedEntity() {
    var globalNameMap = new GlobalNameMap();
    var method = function("Duration", "toMillis");
    assertNull(globalNameMap.addFunction(method));
    assertNull(globalNameMap.lookupEntity(Identifier.ofEntity("toMillis")));
    assertSame(method, globalNameMap.lookupEntity(Identifier.ofMethod("Duration", "toMillis")));
  }

  @Test
  void testCollectTypes() {
    var globalNameMap = new GlobalNameMap();
    var errorConsumer = new InMemoryErrorConsumer();
    var type1 = newRecordType("m1", "Duration");
    var type2 = newRecordType("m2", "Duration");

    assertNull(globalNameMap.addType(type1));
    assertNull(globalNameMap.addType(type2));

    assertNull(globalNameMap.lookupType(Identifier.ofType("Duration")));
    assertSame(type1, globalNameMap.lookupType(Identifier.ofType("Duration").withModule("m1")));
    assertSame(type2, globalNameMap.lookupType(Identifier.ofType("Duration").withModule("m2")));

    var typeMap = globalNameMap.collectTypes(module("m1"), errorConsumer);
    assertEquals(0, errorConsumer.errorCount());
    assertEquals(1, typeMap.size());
    assertSame(type1, typeMap.get("Duration"));
  }

  @Test
  void testCollectTypesReportsDuplicateAndContinues() {
    var globalNameMap = new GlobalNameMap();
    var errorConsumer = new InMemoryErrorConsumer();
    var type1 = newRecordType("m1", "Duration");
    var type2 = newRecordType("m1", "Duration");
    var type3 = newRecordType("m1", "Size");
    renameType(new Identifier("m1", "Duration", "tag1"), 1, type1);
    renameType(new Identifier("m1", "Duration", "tag2"), 2, type2);
    type3.location(new Location(3, 1));

    assertNull(globalNameMap.addType(type1));
    assertNull(globalNameMap.addType(type2));
    assertNull(globalNameMap.addType(type3));

    var typeMap = globalNameMap.collectTypes(module("m1"), errorConsumer);
    assertEquals(
        List.of("Duplicate type: 'm1~Duration.tag2'. Defined at 1,1."), errorConsumer.errors());
    assertEquals(2, typeMap.size());
    assertSame(type1, typeMap.get("Duration"));
    assertSame(type3, typeMap.get("Size"));
  }

  @Test
  void testCollectFunctionsAndVariables() {
    var globalNameMap = new GlobalNameMap();
    var errorConsumer = new InMemoryErrorConsumer();
    var var1 = globalVariable("m1", "x");
    var var2 = globalVariable("m2", "x");
    var function1 = freeFunction("m1", "f");
    var function2 = freeFunction("m2", "f");
    var method = function("Duration", "toMillis");
    method.name(method.name().withModule("m1"));

    assertNull(globalNameMap.addVariable(var1));
    assertNull(globalNameMap.addVariable(var2));
    assertNull(globalNameMap.addFunction(function1));
    assertNull(globalNameMap.addFunction(function2));
    assertNull(globalNameMap.addFunction(method));

    assertNull(globalNameMap.lookupEntity(Identifier.ofEntity("x")));
    assertSame(var1, globalNameMap.lookupEntity(Identifier.ofEntity("x").withModule("m1")));
    assertSame(var2, globalNameMap.lookupEntity(Identifier.ofEntity("x").withModule("m2")));

    var localNameMap = globalNameMap.collectFunctionsAndVariables(module("m1"), errorConsumer);
    assertEquals(0, errorConsumer.errorCount());
    assertSame(var1, localNameMap.lookupEntity(Identifier.ofEntity("x")));
    assertSame(function1, localNameMap.lookupEntity(Identifier.ofEntity("f")));
    assertNull(localNameMap.lookupEntity(Identifier.ofEntity("toMillis")));
  }
}
