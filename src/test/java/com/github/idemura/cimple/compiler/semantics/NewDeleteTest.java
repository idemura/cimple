package com.github.idemura.cimple.compiler.semantics;

import static com.github.idemura.cimple.compiler.ast.AstUtils.*;
import static com.github.idemura.cimple.compiler.parser.Parser.parseCode;
import static org.junit.jupiter.api.Assertions.*;

import com.github.idemura.cimple.compiler.ast.AstNew;
import com.github.idemura.cimple.compiler.ast.AstTypeRef;
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
          return new Duration;
        }
        """;
    var module = parseCode(code, errorConsumer);
    new SemanticAnalyzer(errorConsumer).analyze(module);
    assertEquals(List.of(), errorConsumer.errors());
    {
      var expr = (AstNew) extractReturnExpression(module.findFunction("f"));
      assertEquals(AstTypeRef.ofName("test", "Duration"), expr.typeRef());
      assertSame(module.findType("Duration"), expr.typeRef().type());
      assertNull(expr.size());
    }
  }
}
