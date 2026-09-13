package io.lang.cimple.compiler;

import static io.lang.cimple.compiler.AstUtils.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class ForLoopTest extends AbstractTest {
  @Test
  void testBreakInsideForLoop() {
    var code =
        """
        module test;
        function f() {
          for true {
            break;
          }
        }
        """;
    analyze(code, errorConsumer);
    assertEquals(List.of(), errorConsumer.errors());
  }

  @Test
  void testBreakOutsideForLoop() {
    var code =
        """
        module test;
        function f() {
          break;
        }
        """;
    analyze(code, errorConsumer);
    assertEquals(List.of("'break' is only allowed inside a loop"), errorConsumer.errors());
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
    analyze(code, errorConsumer);
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
    analyze(code, errorConsumer);
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
    analyze(code, errorConsumer);
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
    analyze(code, errorConsumer);
    assertEquals(
        List.of("Duplicate local variable: 'i'. Defined at 3,11."), errorConsumer.errors());
  }
}
