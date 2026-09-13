package io.lang.cimple.compiler;

import static io.lang.cimple.compiler.AstUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.List;

class ArrayTest extends AbstractTest {
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
    assertEquals(new AstArrayType(AstBuiltinType.INT64), local.variable().type());
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
