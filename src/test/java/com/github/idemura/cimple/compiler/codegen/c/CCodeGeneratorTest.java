package com.github.idemura.cimple.compiler.codegen.c;

import static org.junit.jupiter.api.Assertions.*;

import com.github.idemura.cimple.compiler.Compiler;
import com.github.idemura.cimple.compiler.CompilerParams;
import com.github.idemura.cimple.compiler.InMemoryErrorConsumer;
import org.junit.jupiter.api.Test;

class CCodeGeneratorTest {
  private static String compile(String code) {
    var errorConsumer = new InMemoryErrorConsumer();
    var output = new StringBuilder();
    var compiler = new Compiler(new CompilerParams(), errorConsumer, new CCodeGenerator(output));
    assertTrue(compiler.compile("test.ci", code));
    assertEquals(0, errorConsumer.errorCount());
    return output.toString();
  }

  @Test
  void testGenerateRecordType() {
    var code =
        """
        module test;
        type record Point {
          var x int;
          var y int;
          const name string;
        }
        """;
    var output = compile(code);
    System.out.println(output);
    assertTrue(output.contains("struct test__Point;"));
    assertTrue(
        output.contains(
            """
            struct test__Point {
              int64_t x;
              int64_t y;
              char* name;
            };
            """));
  }

  @Test
  void testGenerateRecordTypeWithSelfPointer() {
    var code =
        """
        module test;
        type record Node {
          var next Node*;
        }
        """;
    var output = compile(code);
    System.out.println(output);
    assertTrue(output.contains("struct test__Node;"));
    assertTrue(
        output.contains(
            """
            struct test__Node {
              struct test__Node* next;
            };
            """));
  }
}
