package io.lang.cimple.compiler;

import static org.junit.jupiter.api.Assertions.*;
import com.google.common.collect.ImmutableList;
import io.lang.cimple.compiler.ast.AstBuiltinType;
import io.lang.cimple.compiler.ast.AstFunction;
import io.lang.cimple.compiler.ast.AstFunctionHeader;
import io.lang.cimple.compiler.ast.AstType;
import io.lang.cimple.compiler.ast.AstVariable;
import org.junit.jupiter.api.Test;
import java.util.List;

class FunctionSignatureTest {
  private static AstFunction function(String name, AstType... parameterTypes) {
    return function("test", name, parameterTypes);
  }

  private static AstFunction function(String moduleName, String name, AstType... parameterTypes) {
    var parameters = new ImmutableList.Builder<AstVariable>();
    for (var i = 0; i < parameterTypes.length; i++) {
      var parameter = new AstVariable();
      parameter.name(Identifier.of("p" + i));
      parameter.type(parameterTypes[i]);
      parameters.add(parameter);
    }

    var header = new AstFunctionHeader();
    header.parameters(parameters.build());

    var function = new AstFunction();
    function.name(Identifier.of(name).module(moduleName));
    function.header(header);
    return function;
  }

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
