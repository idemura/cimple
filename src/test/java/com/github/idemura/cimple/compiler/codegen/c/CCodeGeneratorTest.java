package com.github.idemura.cimple.compiler.codegen.c;

import static org.junit.jupiter.api.Assertions.*;

import com.github.idemura.cimple.compiler.Compiler;
import com.github.idemura.cimple.compiler.CompilerParams;
import com.github.idemura.cimple.compiler.InMemoryErrorConsumer;
import java.util.List;
import org.junit.jupiter.api.Test;

class CCodeGeneratorTest {
  private final InMemoryErrorConsumer errorConsumer = new InMemoryErrorConsumer();
  private final CCodeGeneratorParams.Builder codegenParamsBuilder =
      CCodeGeneratorParams.builder().outputPreamble(false);

  private String compile(String code) {
    var output = new StringBuilder();
    var codegen = new CCodeGenerator(codegenParamsBuilder.build(), output);
    var compilerParams = CompilerParams.builder().build();
    var compiler = new Compiler(compilerParams, errorConsumer, codegen);
    assertTrue(compiler.compile(List.of(code)));
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
    assertEquals(
        """
        struct test__Point;

        struct test__Point {
          int64_t x;
          int64_t y;
          char* name;
        };

        """,
        output);
  }

  @Test
  void testGenerateRecordTypeWithoutModuleNameMangle() {
    var code =
        """
        module test;
        type record Point {
          var x int;
        }
        """;
    codegenParamsBuilder.mangleModuleName(false);
    var output = compile(code);
    assertEquals(
        """
        struct Point;

        struct Point {
          int64_t x;
        };

        """,
        output);
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
    assertEquals(
        """
        struct test__Node;

        struct test__Node {
          struct test__Node* next;
        };

        """,
        output);
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
    assertEquals(
        """
        enum test__Color {
          test__Color_Red,
          test__Color_Green,
          test__Color_Blue,
        };

        """,
        output);
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
    assertEquals(
        """
        enum test__Maybe_tag_ {
          test__Maybe_None,
          test__Maybe_Some,
          test__Maybe_Error,
        };
        struct test__Maybe {
          enum test__Maybe_tag_ tag;
          union {
            int64_t Some;
            char* Error;
          } u;
        };

        """,
        output);
  }
}
