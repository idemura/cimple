package io.lang.cimple.compiler;

import static io.lang.cimple.compiler.AstUtils.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class FunctionSignatureTest {
  @Test
  void testFromFunction() {
    var function = function("copy", AstBuiltinType.INT64, AstBuiltinType.BOOL);
    var signature = function.signature();

    assertEquals("copy", signature.name());
    assertEquals(List.of(AstBuiltinType.INT64, AstBuiltinType.BOOL), signature.parameterTypes());
  }

  @Test
  void testEqualsUsesNameAndParameterTypes() {
    var base = function("copy", AstBuiltinType.INT64).signature();
    var same = function("copy", AstBuiltinType.INT64).signature();
    var differentName = function("move", AstBuiltinType.INT64).signature();
    var differentParameters = function("copy", AstBuiltinType.BOOL).signature();

    assertEquals(base, same);
    assertEquals(base.hashCode(), same.hashCode());
    assertNotEquals(base, differentName);
    assertNotEquals(base, differentParameters);
  }

  @Test
  void testEqualsDoesNotUseModule() {
    var m1 = function("m1", "copy", AstBuiltinType.INT64).signature();
    var m2 = function("m2", "copy", AstBuiltinType.INT64).signature();
    assertEquals(m1, m2);
  }
}
