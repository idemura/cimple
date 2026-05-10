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
        function f() {
          var x = 1 + 2;
        }
        """;
    var module = parseCode(code, errorConsumer);
    new SemanticAnalyzer(errorConsumer).analyze(module);
    assertEquals(List.of(), errorConsumer.errors());
  }
}
