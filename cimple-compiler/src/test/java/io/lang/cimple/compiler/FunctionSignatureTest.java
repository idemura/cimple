package io.lang.cimple.compiler;

import static org.junit.jupiter.api.Assertions.*;
import io.lang.cimple.compiler.ast.AstBuiltinType;
import io.lang.cimple.compiler.ast.AstFunction;
import io.lang.cimple.compiler.ast.AstFunctionHeader;
import io.lang.cimple.compiler.ast.AstType;
import io.lang.cimple.compiler.ast.AstVariable;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

class FunctionSignatureTest {
  private static AstFunction function(String name, AstType... parameterTypes) {
    var parameters = new ArrayList<AstVariable>();
    for (var i = 0; i < parameterTypes.length; i++) {
      var parameter = new AstVariable();
      parameter.name(Identifier.ofEntity("p" + i));
      parameter.type(parameterTypes[i]);
      parameters.add(parameter);
    }

    var header = new AstFunctionHeader();
    header.parameters(parameters);

    var function = new AstFunction();
    function.name(Identifier.ofEntity(name).withModule("test"));
    function.header(header);
    return function;
  }

  @Test
  void testFromFunction() {
    var function = function("copy", AstBuiltinType.INT64, AstBuiltinType.BOOL);
    var signature = FunctionSignature.of(function);

    assertEquals(Identifier.ofEntity("copy").withModule("test"), signature.name());
    assertEquals(List.of(AstBuiltinType.INT64, AstBuiltinType.BOOL), signature.parameterTypes());
  }

  @Test
  void testEqualsUsesNameAndParameterTypes() {
    var base = FunctionSignature.of(function("copy", AstBuiltinType.INT64));
    var same = FunctionSignature.of(function("copy", AstBuiltinType.INT64));
    var differentName = FunctionSignature.of(function("move", AstBuiltinType.INT64));
    var differentParameters = FunctionSignature.of(function("copy", AstBuiltinType.BOOL));

    assertEquals(base, same);
    assertEquals(base.hashCode(), same.hashCode());
    assertNotEquals(base, differentName);
    assertNotEquals(base, differentParameters);
  }

  @Test
  void testConstructorCopiesParameterTypes() {
    var parameterTypes = new ArrayList<AstType>();
    parameterTypes.add(AstBuiltinType.INT64);
    var signature = new FunctionSignature(Identifier.ofEntity("f"), parameterTypes);

    parameterTypes.add(AstBuiltinType.BOOL);

    assertEquals(List.of(AstBuiltinType.INT64), signature.parameterTypes());
  }
}
