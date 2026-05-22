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
    var compilerParams = CompilerParams.builder().build();
    var compiler = new Compiler(compilerParams, errorConsumer, new CCodeGenerator(output));
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
    assertTrue(output.contains("struct test__Node;"));
    assertTrue(
        output.contains(
            """
            struct test__Node {
              struct test__Node* next;
            };
            """));
  }

  @Test
  void testGenerateDataLessUnionType() {
    var code =
        """
        module test;
        type union Color {
          Red;
          Green;
          Blue;
        }
        """;
    var output = compile(code);
    assertTrue(
        output.contains(
            """
            enum test__Color {
              test__Color_Red,
              test__Color_Green,
              test__Color_Blue,
            };
            """));
  }

  @Test
  void testGenerateTaggedUnionType() {
    var code =
        """
        module test;
        type union Maybe {
          None;
          Some(int);
          Error(string);
        }
        """;
    var output = compile(code);
    assertTrue(
        output.contains(
            """
            enum test__Maybe_tag_ {
              test__Maybe_None,
              test__Maybe_Some,
              test__Maybe_Error,
            };
            """));
    assertTrue(
        output.contains(
            """
            struct test__Maybe {
              enum test__Maybe_tag_ tag;
              union {
                int64_t Some;
                char* Error;
              } u;
            };
            """));
  }
}
