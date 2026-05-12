package com.github.idemura.cimple.compiler.semantics;

import static com.github.idemura.cimple.compiler.parser.Parser.parseCode;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class ExpressionTest extends AbstractSemanticsTest {
  @Test
  void testExpression() {
    var code =
        """
        module test;
        type record Duration {
          var seconds int;
        }
        function f(d Duration) {
          var x = 1 + 2;
          var y = d.seconds;
        }
        """;
    var module = parseCode(code);
    var sa = new SemanticAnalyzer(errorConsumer);
    sa.analyze(module);
    assertEquals(List.of(), errorConsumer.errors());
  }

  @Test
  void testInvalidFieldAccess() {
    var code =
        """
        module test;
        type record Duration {
          var seconds int;
        }
        function f() {
          var x = 1.seconds;
          var d Duration;
          var y = d.millis;
        }
        """;
    var module = parseCode(code);
    var sa = new SemanticAnalyzer(errorConsumer);
    sa.analyze(module);
    assertEquals(
        List.of(
            "Field access requires a record, got 'int64'",
            "Undefined field 'millis' in record 'test~Duration'"),
        errorConsumer.errors());
  }
}
