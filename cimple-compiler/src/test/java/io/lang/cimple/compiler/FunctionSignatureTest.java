package io.lang.cimple.compiler;

import static io.lang.cimple.compiler.AstTreeUtils.wildcard;
import static org.junit.jupiter.api.Assertions.*;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

class FunctionSignatureTest {
  private AstFunctionType functionType(
      String name, AstType resultType, AstType... parameterTypes) {
    var parameters = new ImmutableList.Builder<AstVariable>();
    for (var i = 0; i < parameterTypes.length; i++) {
      parameters.add(
          new AstVariable(new Identifier("p" + i), parameterTypes[i], null));
    }
    return new AstFunctionType(
        new AstFunction(
            new Identifier(name), ImmutableList.of(), parameters.build(), resultType, null));
  }

  @Test
  void testEqualsUsesNameAndParameterTypes() {
    var base = new FunctionSignature("copy", ImmutableList.of(AstBuiltinType.INT64));
    var same = new FunctionSignature("copy", ImmutableList.of(AstBuiltinType.INT64));
    var differentName = new FunctionSignature("move", ImmutableList.of(AstBuiltinType.INT64));
    var differentParameters = new FunctionSignature("copy", ImmutableList.of(AstBuiltinType.BOOL));

    assertEquals(base, same);
    assertEquals(base.hashCode(), same.hashCode());
    assertNotEquals(base, differentName);
    assertNotEquals(base, differentParameters);
  }

  @Test
  void testArrayHashUsesTypeFamily() {
    var intArray =
        new FunctionSignature("copy", ImmutableList.of(new AstArrayType(AstBuiltinType.INT64)));
    var boolArray =
        new FunctionSignature("copy", ImmutableList.of(new AstArrayType(AstBuiltinType.BOOL)));

    assertNotEquals(intArray, boolArray);
    assertEquals(intArray.hashCode(), boolArray.hashCode());
  }

  @Test
  void testArrayAndPointerTypesCompareTheirBaseTypes() {
    var wildcardPointer =
        new FunctionSignature(
            "copy", ImmutableList.of(new AstPointerType(wildcard("T"))));
    var intPointer =
        new FunctionSignature(
            "copy", ImmutableList.of(new AstPointerType(AstBuiltinType.INT64)));
    var intArray =
        new FunctionSignature(
            "copy", ImmutableList.of(new AstArrayType(AstBuiltinType.INT64)));

    assertEquals(wildcardPointer, intPointer);
    assertEquals(wildcardPointer.hashCode(), intPointer.hashCode());
    assertNotEquals(intPointer, intArray);
  }

  @Test
  void testFunctionTypesCompareSignatureAndResultType() {
    var base =
        new FunctionSignature(
            "accept",
            ImmutableList.of(
                functionType("Callback", AstBuiltinType.BOOL, AstBuiltinType.INT64)));
    var same =
        new FunctionSignature(
            "accept",
            ImmutableList.of(
                functionType("Callback", AstBuiltinType.BOOL, AstBuiltinType.INT64)));
    var differentParameter =
        new FunctionSignature(
            "accept",
            ImmutableList.of(
                functionType("Callback", AstBuiltinType.BOOL, AstBuiltinType.CHAR)));
    var differentResult =
        new FunctionSignature(
            "accept",
            ImmutableList.of(
                functionType("Callback", AstBuiltinType.INT64, AstBuiltinType.INT64)));

    assertEquals(base, same);
    assertNotEquals(base, differentParameter);
    assertNotEquals(base, differentResult);
  }

  @Test
  void testWildcardArrayEqualsConcreteArray() {
    var generic = new FunctionSignature("size", ImmutableList.of(new AstArrayType(wildcard("T"))));
    var concrete =
        new FunctionSignature("size", ImmutableList.of(new AstArrayType(AstBuiltinType.INT64)));

    assertEquals(generic, concrete);
    assertEquals(concrete, generic);
    assertEquals(generic.hashCode(), concrete.hashCode());
    assertEquals(AstBuiltinType.INT64, generic.match(concrete).get(wildcard("T")));
  }

  @Test
  void testWildcardEqualsAnyType() {
    var generic = new FunctionSignature("convert", ImmutableList.of(wildcard("T")));
    var concrete = new FunctionSignature("convert", ImmutableList.of(AstBuiltinType.INT64));

    assertEquals(generic, concrete);
    assertEquals(concrete, generic);
    assertEquals(generic.hashCode(), concrete.hashCode());
  }

  @Test
  void testRepeatedWildcardConsistencyIsCheckedAfterLookup() {
    var generic = new FunctionSignature("same", ImmutableList.of(wildcard("T"), wildcard("T")));
    var matching =
        new FunctionSignature("same", ImmutableList.of(AstBuiltinType.INT64, AstBuiltinType.INT64));
    var conflicting =
        new FunctionSignature("same", ImmutableList.of(AstBuiltinType.INT64, AstBuiltinType.BOOL));

    assertEquals(generic, matching);
    assertEquals(generic, conflicting);
    assertNull(generic.match(conflicting));
  }
}
