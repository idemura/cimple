package com.github.idemura.cimple.compiler;

import static com.github.idemura.cimple.compiler.ast.AstUtils.*;
import static org.junit.jupiter.api.Assertions.*;

import com.github.idemura.cimple.compiler.ast.AstArrayAccess;
import com.github.idemura.cimple.compiler.ast.AstArrayType;
import com.github.idemura.cimple.compiler.ast.AstBuiltinType;
import com.github.idemura.cimple.compiler.ast.AstCall;
import com.github.idemura.cimple.compiler.ast.AstEntityRef;
import com.github.idemura.cimple.compiler.ast.AstExpressionStatement;
import com.github.idemura.cimple.compiler.ast.AstFunction;
import com.github.idemura.cimple.compiler.ast.AstLocal;
import com.github.idemura.cimple.compiler.ast.AstType;
import java.util.List;
import org.junit.jupiter.api.Test;

class ArrayTest extends AbstractSemanticsTest {
  private static void assertArraySizeCall(AstFunction function, AstType expectedObjectType) {
    assertArrayMethodCall(function, 0, BuiltinFunctions.ARRAY_SIZE, expectedObjectType);
  }

  private static void assertArrayMethodCall(
      AstFunction function, int statementIndex, AstFunction method, AstType expectedObjectType) {
    var local = (AstLocal) function.block().statements().get(statementIndex);
    assertEquals(AstBuiltinType.INT64, local.variable().type());

    var call = (AstCall) local.variable().expression().get();
    var functionRef = (AstEntityRef) call.function();
    assertSame(method, functionRef.entity());
    assertEquals(AstBuiltinType.INT64, call.type());

    assertEquals(1, method.header().parameters().size());
    assertEquals(1, call.arguments().size());
    var object = (AstEntityRef) call.arguments().get(0);
    assertSame(function.header().parameters().get(0), object.entity());
    assertEquals(expectedObjectType, object.type());
  }

  private static void assertArrayAppendCall(
      AstFunction function, int statementIndex, int objectIndex, AstArrayType expectedObjectType) {
    var statement = (AstExpressionStatement) function.block().statements().get(statementIndex);
    var call = (AstCall) statement.expression().get();
    var functionRef = (AstEntityRef) call.function();
    assertSame(BuiltinFunctions.ARRAY_APPEND, functionRef.entity());
    assertEquals(AstBuiltinType.VOID, call.type());

    assertEquals(2, call.arguments().size());
    var object = (AstEntityRef) call.arguments().get(0);
    assertSame(function.header().parameters().get(objectIndex), object.entity());
    assertEquals(expectedObjectType, object.type());
    assertEquals(expectedObjectType.baseType(), call.arguments().get(1).type());
  }

  private static void assertArrayAccess(AstFunction function, AstType expectedElementType) {
    var local = (AstLocal) function.block().statements().get(0);
    assertEquals(expectedElementType, local.variable().type());

    var access = (AstArrayAccess) local.variable().expression().get();
    assertEquals(expectedElementType, access.type());

    var array = (AstEntityRef) access.array();
    assertSame(function.header().parameters().get(0), array.entity());
    assertEquals(arrayType(expectedElementType), array.type());
    assertEquals(AstBuiltinType.INT64, access.index().type());
  }

  @Test
  void testArraySizeAcceptsArrayOfAnyElementType() {
    var code =
        """
        module test;
        type struct Point {}
        function f(values int[]) {
          var n = values.size();
        }
        function g(points Point[]) {
          var n = points.size();
        }
        """;
    var module = parseCode(code);
    new SemanticAnalyzer(errorConsumer).analyze(List.of(module));
    assertEquals(List.of(), errorConsumer.errors());

    assertArraySizeCall(module.findFunction("f"), arrayType(AstBuiltinType.INT64));
    assertArraySizeCall(module.findFunction("g"), arrayType(newStructType("test", "Point")));
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
    new SemanticAnalyzer(errorConsumer).analyze(List.of(module));
    assertEquals(List.of(), errorConsumer.errors());

    assertArraySizeCall(module.findFunction("f"), arrayType(AstBuiltinType.INT64));
  }

  @Test
  void testArrayMethodsRejectInvalidArguments() {
    var code =
        """
        module test;
        function f(values int[]) {
          var n = values.size(1);
          var c = values.capacity(1);
          values.append();
          values.append(true);
        }
        """;
    var module = parseCode(code);
    new SemanticAnalyzer(errorConsumer).analyze(List.of(module));
    assertEquals(
        List.of(
            "Array method 'size' expects 0 arguments, got 1",
            "Array method 'capacity' expects 0 arguments, got 1",
            "Array method 'append' expects 1 arguments, got 0",
            "Array method 'append' argument has type 'bool', expected 'int64'"),
        errorConsumer.errors());
  }

  @Test
  void testArrayAccessType() {
    var code =
        """
        module test;
        type struct Point {}
        function f(values int[]) {
          var x = values[0];
        }
        function g(points Point[]) {
          var p = points[0];
        }
        """;
    var module = parseCode(code);
    new SemanticAnalyzer(errorConsumer).analyze(List.of(module));
    assertEquals(List.of(), errorConsumer.errors());

    assertArrayAccess(module.findFunction("f"), AstBuiltinType.INT64);
    assertArrayAccess(module.findFunction("g"), newStructType("test", "Point"));
  }

  @Test
  void testArrayCapacityAndAppendAcceptArrayOfAnyElementType() {
    var code =
        """
        module test;
        type struct Point {}
        function f(values int[], points Point[], point Point) {
          var capacity = values.capacity();
          values.append(1);
          points.append(point);
        }
        """;
    var module = parseCode(code);
    new SemanticAnalyzer(errorConsumer).analyze(List.of(module));
    assertEquals(List.of(), errorConsumer.errors());

    var function = module.findFunction("f");
    assertEquals(3, function.block().statements().size());
    assertArrayMethodCall(
        function, 0, BuiltinFunctions.ARRAY_CAPACITY, arrayType(AstBuiltinType.INT64));
    assertArrayAppendCall(function, 1, 0, arrayType(AstBuiltinType.INT64));
    assertArrayAppendCall(function, 2, 1, arrayType(newStructType("test", "Point")));
  }

  @Test
  void testNewArrayAndDeleteArray() {
    var code =
        """
        module test;
        function f() {
          var values = new int[](5);
          delete values;
        }
        """;
    var module = parseCode(code);
    new SemanticAnalyzer(errorConsumer).analyze(List.of(module));
    assertEquals(List.of(), errorConsumer.errors());

    var statements = module.findFunction("f").block().statements();
    assertEquals(2, statements.size());
    var local = (AstLocal) statements.get(0);
    assertEquals(arrayType(AstBuiltinType.INT64), local.variable().type());
  }

  @Test
  void testArrayAccessTypeErrors() {
    var code =
        """
        module test;
        function f(values int[], x int) {
          var a = x[0];
          var b = values[true];
        }
        """;
    var module = parseCode(code);
    new SemanticAnalyzer(errorConsumer).analyze(List.of(module));
    assertEquals(
        List.of(
            "Array access requires an array, got 'int64'",
            "Array index has type 'bool', expected 'int64'"),
        errorConsumer.errors());
  }
}
