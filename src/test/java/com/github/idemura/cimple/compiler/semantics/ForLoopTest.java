package com.github.idemura.cimple.compiler.semantics;

import static com.github.idemura.cimple.compiler.parser.Parser.parseCode;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class ForLoopTest extends AbstractSemanticsTest {
  private void analyze(String code) {
    var module = parseCode(code);
    new SemanticAnalyzer(errorConsumer).analyze(module);
  }

  @Test
  void testForLoopVariable() {
    var code =
        """
        module test;
        function f() {
          for var i = 0; i < 3; i += 1 {
          }
        }
        """;
    analyze(code);
    assertEquals(List.of(), errorConsumer.errors());
  }

  @Test
  void testForLoopVariableDoesNotShadowParameter() {
    var code =
        """
        module test;
        function f(i int) {
          for var i = 0; i < 3; i += 1 {
          }
        }
        """;
    analyze(code);
    assertEquals(
        List.of("Duplicate local variable: 'i'. Defined at 2,12."), errorConsumer.errors());
  }

  @Test
  void testForLoopVariableDoesNotShadowLocal() {
    var code =
        """
        module test;
        function f() {
          var i = 0;
          for var i = 0; i < 3; i += 1 {
          }
        }
        """;
    analyze(code);
    assertEquals(List.of("Duplicate local variable: 'i'. Defined at 3,7."), errorConsumer.errors());
  }

  @Test
  void testLocalDoesNotShadowForLoopVariable() {
    var code =
        """
        module test;
        function f() {
          for var i = 0; i < 3; i += 1 {
            var i = 1;
          }
        }
        """;
    analyze(code);
    assertEquals(
        List.of("Duplicate local variable: 'i'. Defined at 3,11."), errorConsumer.errors());
  }
}
