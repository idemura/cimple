package com.github.idemura.cimple.compiler.semantics;

import static com.github.idemura.cimple.compiler.ast.AstUtils.*;
import static org.junit.jupiter.api.Assertions.*;

import com.github.idemura.cimple.compiler.ast.AstLocal;
import java.util.List;
import org.junit.jupiter.api.Test;

class NewDeleteTest extends AbstractSemanticsTest {
  @Test
  void testNewExpression() {
    var code =
        """
        module test;
        type record Duration {
          var seconds int;
        }
        function f() {
          var d = new Duration;
        }
        """;
    var module = parseCode(code);
    var sa = new SemanticAnalyzer(errorConsumer);
    sa.analyze(module);
    assertEquals(List.of(), errorConsumer.errors());
    var statements = module.findFunction("f").block().statements();
    {
      var stmt = (AstLocal) statements.get(0);
      assertEquals(pointerType(newRecordType("test", "Duration")), stmt.variable().type());
    }
  }
}
