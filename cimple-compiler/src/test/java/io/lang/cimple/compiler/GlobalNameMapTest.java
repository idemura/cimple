package io.lang.cimple.compiler;

import static io.lang.cimple.compiler.AstTreeUtils.*;
import static org.junit.jupiter.api.Assertions.*;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.jupiter.api.Test;

class GlobalNameMapTest {
  private AstFunction genericArrayFunction(String moduleName, String functionName, String typeName) {
    var wildcard = wildcard(typeName);
    return new AstFunction(
        entityName(moduleName, functionName),
        ImmutableList.of(wildcard),
        ImmutableList.of(variable(null, "a", AstVariable.PARAMETER, new AstArrayType(wildcard))),
        null,
        null);
  }

  @Test
  void testCollectTypes() {
    var globalNameMap = new GlobalNameMap();
    var errorConsumer = new ErrorConsumer();
    var type1 = newStructType("m1", "Duration");
    var type2 = newStructType("m2", "Duration");

    assertNull(globalNameMap.addType(type1));
    assertNull(globalNameMap.addType(type2));

    assertNull(globalNameMap.lookupType(new Identifier("Duration")));
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
    assertNull(globalNameMap.addType(type1));
    assertSame(type1, globalNameMap.addType(type2));
    assertNull(globalNameMap.addType(type3));

    var typeMap = globalNameMap.collectTypes("m1", errorConsumer);
    assertEquals(List.of(), errorConsumer.errors());
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

    assertNull(globalNameMap.lookupVariable(new Identifier("x")));
    assertSame(var1, globalNameMap.lookupVariable(entityName("m1", "x")));
    assertSame(var2, globalNameMap.lookupVariable(entityName("m2", "x")));

    var localNameMap = globalNameMap.collectVariables("m1", errorConsumer);
    assertEquals(0, errorConsumer.errorCount());
    assertSame(var1, localNameMap.lookupVariable("x"));
  }

  @Test
  void testFunctionNamespaceIsGlobal() {
    var globalNameMap = new GlobalNameMap();
    var function1 = freeFunction("m1", "f");
    var function2 = freeFunction("m2", "f");

    assertNull(globalNameMap.addFunction(function1));
    assertSame(function1, globalNameMap.addFunction(function2));

    assertSame(function1, globalNameMap.lookupFunction(function1.signature()));
  }

  @Test
  void testFunctionMapUsesSignature() {
    var globalNameMap = new GlobalNameMap();
    var function1 = freeFunction("m1", "f", AstBuiltinType.INT64);
    var function2 = freeFunction("m1", "f", AstBuiltinType.BOOL);

    assertNull(globalNameMap.addFunction(function1));
    assertNull(globalNameMap.addFunction(function2));

    assertSame(function1, globalNameMap.lookupFunction(function1.signature()));
    assertSame(function2, globalNameMap.lookupFunction(function2.signature()));
  }

  @Test
  void testDuplicateFunctionSignature() {
    var globalNameMap = new GlobalNameMap();
    var function1 = freeFunction("m1", "f", AstBuiltinType.INT64);
    var function2 = freeFunction("m1", "f", AstBuiltinType.INT64);

    assertNull(globalNameMap.addFunction(function1));
    assertSame(function1, globalNameMap.addFunction(function2));
  }

  @Test
  void testGenericArraySignatureCollidesWithConcreteArraySignature() {
    var globalNameMap = new GlobalNameMap();
    var concrete = freeFunction("m1", "f", new AstArrayType(AstBuiltinType.INT64));
    var generic = genericArrayFunction("m1", "f", "T");

    assertNull(globalNameMap.addFunction(concrete));
    assertSame(concrete, globalNameMap.addFunction(generic));
  }

  @Test
  void testEquivalentGenericArraySignaturesCollide() {
    var globalNameMap = new GlobalNameMap();
    var first = genericArrayFunction("m1", "f", "T");
    var second = genericArrayFunction("m1", "f", "S");

    assertNull(globalNameMap.addFunction(first));
    assertSame(first, globalNameMap.addFunction(second));
  }

  @Test
  void testConcreteArraySignaturesCanCoexist() {
    var globalNameMap = new GlobalNameMap();
    var intArray = freeFunction("m1", "f", new AstArrayType(AstBuiltinType.INT64));
    var boolArray = freeFunction("m1", "f", new AstArrayType(AstBuiltinType.BOOL));

    assertNull(globalNameMap.addFunction(intArray));
    assertNull(globalNameMap.addFunction(boolArray));
    assertSame(intArray, globalNameMap.lookupFunction(intArray.signature()));
    assertSame(boolArray, globalNameMap.lookupFunction(boolArray.signature()));
  }
}
