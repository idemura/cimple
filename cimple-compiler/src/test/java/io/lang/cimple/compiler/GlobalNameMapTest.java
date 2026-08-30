package io.lang.cimple.compiler;

import static io.lang.cimple.compiler.ast.AstUtils.function;
import static io.lang.cimple.compiler.ast.AstUtils.globalVariable;
import static io.lang.cimple.compiler.ast.AstUtils.newStructType;
import static io.lang.cimple.compiler.ast.AstUtils.parameter;
import static org.junit.jupiter.api.Assertions.*;
import io.lang.cimple.compiler.ast.AstBuiltinType;
import io.lang.cimple.compiler.ast.AstFunction;
import io.lang.cimple.compiler.ast.AstType;
import io.lang.cimple.compiler.ast.AstVariable;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

class GlobalNameMapTest {
  // TODO Move into AstUtils?
  private static AstFunction freeFunction(
      String moduleName, String name, AstType... parameterTypes) {
    var function = function(name);
    function.name(entityName(moduleName, name));
    function.header().parameters(parameters(parameterTypes));
    return function;
  }

  private static List<AstVariable> parameters(AstType... parameterTypes) {
    var parameters = new ArrayList<AstVariable>();
    for (var i = 0; i < parameterTypes.length; i++) {
      var parameter = parameter("p" + i);
      parameter.type(parameterTypes[i]);
      parameters.add(parameter);
    }
    return parameters;
  }

  private static void renameType(Identifier name, int line, AstType type) {
    type.name(name);
    type.location(new Location(line, 1));
  }

  private static Identifier typeName(String moduleName, String name) {
    return Identifier.ofType(name).module(moduleName);
  }

  private static Identifier entityName(String moduleName, String name) {
    return Identifier.of(name).module(moduleName);
  }

  @Test
  void testCollectTypes() {
    var globalNameMap = new GlobalNameMap();
    var errorConsumer = new ErrorConsumer();
    var type1 = newStructType("m1", "Duration");
    var type2 = newStructType("m2", "Duration");

    assertNull(globalNameMap.addType(type1));
    assertNull(globalNameMap.addType(type2));

    assertNull(globalNameMap.lookupType(Identifier.ofType("Duration")));
    assertSame(type1, globalNameMap.lookupType(typeName("m1", "Duration")));
    assertSame(type2, globalNameMap.lookupType(typeName("m2", "Duration")));

    var typeMap = globalNameMap.collectTypes("m1", errorConsumer);
    assertEquals(0, errorConsumer.errorCount());
    assertEquals(1, typeMap.size());
    assertSame(type1, typeMap.get("Duration"));
  }

  @Test
  void testCollectTypesReportsDuplicateAndContinues() {
    var globalNameMap = new GlobalNameMap();
    var errorConsumer = new ErrorConsumer();
    var type1 = newStructType("m1", "Duration");
    var type2 = newStructType("m1", "Duration");
    var type3 = newStructType("m1", "Size");
    renameType(Identifier.ofType("Duration").module("m1").entity("tag1"), 1, type1);
    renameType(Identifier.ofType("Duration").module("m1").entity("tag2"), 2, type2);
    type3.location(new Location(3, 1));

    assertNull(globalNameMap.addType(type1));
    assertNull(globalNameMap.addType(type2));
    assertNull(globalNameMap.addType(type3));

    var typeMap = globalNameMap.collectTypes("m1", errorConsumer);
    assertEquals(
        List.of("Duplicate type: 'm1~Duration.tag2'. Defined at 1,1."), errorConsumer.errors());
    assertEquals(2, typeMap.size());
    assertSame(type1, typeMap.get("Duration"));
    assertSame(type3, typeMap.get("Size"));
  }

  @Test
  void testCollectVariables() {
    var globalNameMap = new GlobalNameMap();
    var errorConsumer = new ErrorConsumer();
    var var1 = globalVariable("m1", "x");
    var var2 = globalVariable("m2", "x");

    assertNull(globalNameMap.addVariable(var1));
    assertNull(globalNameMap.addVariable(var2));

    assertNull(globalNameMap.lookupVariable(Identifier.of("x")));
    assertSame(var1, globalNameMap.lookupVariable(entityName("m1", "x")));
    assertSame(var2, globalNameMap.lookupVariable(entityName("m2", "x")));

    var localNameMap = globalNameMap.collectVariables("m1", errorConsumer);
    assertEquals(0, errorConsumer.errorCount());
    assertSame(var1, localNameMap.lookupVariable("x"));
  }

  @Test
  void testLookupFunctions() {
    var globalNameMap = new GlobalNameMap();
    var function1 = freeFunction("m1", "f");
    var function2 = freeFunction("m2", "f");

    assertNull(globalNameMap.addFunction(function1));
    assertNull(globalNameMap.addFunction(function2));

    assertNull(globalNameMap.lookupFunction(null, function1.signature()));
    assertSame(function1, globalNameMap.lookupFunction("m1", function1.signature()));
    assertSame(function2, globalNameMap.lookupFunction("m2", function2.signature()));
  }

  @Test
  void testFunctionMapUsesSignature() {
    var globalNameMap = new GlobalNameMap();
    var function1 = freeFunction("m1", "f", AstBuiltinType.INT64);
    var function2 = freeFunction("m1", "f", AstBuiltinType.BOOL);

    assertNull(globalNameMap.addFunction(function1));
    assertNull(globalNameMap.addFunction(function2));

    assertSame(function1, globalNameMap.lookupFunction(null, function1.signature()));
    assertSame(function2, globalNameMap.lookupFunction(null, function2.signature()));
    assertSame(function1, globalNameMap.lookupFunction("m1", function1.signature()));
    assertNull(globalNameMap.lookupFunction("m2", function1.signature()));
  }

  @Test
  void testDuplicateFunctionSignature() {
    var globalNameMap = new GlobalNameMap();
    var function1 = freeFunction("m1", "f", AstBuiltinType.INT64);
    var function2 = freeFunction("m1", "f", AstBuiltinType.INT64);

    assertNull(globalNameMap.addFunction(function1));
    assertSame(function1, globalNameMap.addFunction(function2));
  }
}
