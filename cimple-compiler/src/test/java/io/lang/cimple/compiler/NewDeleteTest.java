package io.lang.cimple.compiler;

import static io.lang.cimple.compiler.AstTreeUtils.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class NewDeleteTest extends AbstractTest {
  @Test
  void testNewExpression() {
    var code =
        """
        module test;
        type struct Duration {
          var seconds int;
        }
        function f() {
          var d = new Duration();
          delete d;
          delete new Duration();
        }
        """;
    var module = parseCode(code);
    var sa = new SemanticAnalyzer(errorConsumer);
    sa.analyze(List.of(module));
    assertEquals(List.of(), errorConsumer.errors());
    var statements = module.findFunction("f").block().statements();
    {
      var stmt = (AstLocal) statements.get(0);
      assertEquals(new AstPointerType(newStructType("test", "Duration")), stmt.variable().type());
    }
  }

  @Test
  void testDeleteNotAPointer() {
    var code =
        """
        module test;
        type struct Duration {
          var seconds int;
        }
        function f(n int) {
          delete n;
          delete 1;
          var r Duration;
          delete r;
        }
        """;
    var module = parseCode(code);
    var sa = new SemanticAnalyzer(errorConsumer);
    sa.analyze(List.of(module));
    assertEquals(
        List.of(
            "Delete expression of type 'int64', expected pointer",
            "Delete expression of type 'int64', expected pointer",
            "Delete expression of type 'test~Duration', expected pointer"),
        errorConsumer.errors());
  }
}
