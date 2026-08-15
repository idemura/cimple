package com.github.idemura.cimple.compiler.semantics;

import static com.github.idemura.cimple.compiler.ast.AstUtils.*;
import static org.junit.jupiter.api.Assertions.*;

import com.github.idemura.cimple.compiler.ast.AstBuiltinType;
import com.github.idemura.cimple.compiler.ast.AstCall;
import com.github.idemura.cimple.compiler.ast.AstEntityRef;
import com.github.idemura.cimple.compiler.ast.AstFunction;
import com.github.idemura.cimple.compiler.ast.AstLocal;
import com.github.idemura.cimple.compiler.ast.AstType;
import java.util.List;
import org.junit.jupiter.api.Test;

class ArrayTest extends AbstractSemanticsTest {
  private static void assertArraySizeCall(AstFunction function, AstType expectedObjectType) {
    var local = (AstLocal) function.block().statements().get(0);
    assertEquals(AstBuiltinType.INT64, local.variable().type());

    var call = (AstCall) local.variable().expression().get();
    var functionRef = (AstEntityRef) call.function();
    assertSame(BuiltinFunctions.ARRAY_SIZE, functionRef.entity());
    assertEquals(AstBuiltinType.INT64, call.type());

    assertEquals(1, call.arguments().size());
    var receiver = (AstEntityRef) call.arguments().get(0);
    assertSame(function.header().parameters().get(0), receiver.entity());
    assertEquals(expectedObjectType, receiver.type());
  }

  @Test
  void testArraySizeAcceptsArrayOfAnyElementType() {
    var code =
        """
        module test;
        type record Point {}
        function f(values int[]) {
          var n = values.size();
        }
        function g(points Point[]) {
          var n = points.size();
        }
        """;
    var module = parseCode(code);
    new SemanticAnalyzer(errorConsumer).analyze(module);
    assertEquals(List.of(), errorConsumer.errors());

    assertArraySizeCall(module.findFunction("f"), arrayType(AstBuiltinType.INT64));
    assertArraySizeCall(module.findFunction("g"), arrayType(newRecordType("test", "Point")));
  }

  @Test
  void testArraySizeDoesNotReserveFreeFunctionName() {
    var code =
        """
        module test;
        function size(values int[]) int {
          return 1;
        }
        function f(values int[]) {
          var n = values.size();
        }
        """;
    var module = parseCode(code);
    new SemanticAnalyzer(errorConsumer).analyze(module);
    assertEquals(List.of(), errorConsumer.errors());

    assertArraySizeCall(module.findFunction("f"), arrayType(AstBuiltinType.INT64));
  }

  @Test
  void testArraySizeRejectsArguments() {
    var code =
        """
        module test;
        function f(values int[]) {
          var n = values.size(1);
        }
        """;
    var module = parseCode(code);
    new SemanticAnalyzer(errorConsumer).analyze(module);
    assertEquals(List.of("Array method 'size' expects 0 arguments, got 1"), errorConsumer.errors());
  }
}
