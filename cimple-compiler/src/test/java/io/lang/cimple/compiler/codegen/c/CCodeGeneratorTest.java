package io.lang.cimple.compiler.codegen.c;

import static org.junit.jupiter.api.Assertions.*;

import io.lang.cimple.compiler.Compiler;
import io.lang.cimple.compiler.CompilerParams;
import io.lang.cimple.compiler.ErrorConsumer;
import io.lang.cimple.compiler.SourceCode;
import java.util.List;
import org.junit.jupiter.api.Test;

class CCodeGeneratorTest {
  private final ErrorConsumer errorConsumer = new ErrorConsumer();
  private final CCodeGeneratorParams.Builder codegenParamsBuilder =
      CCodeGeneratorParams.builder().outputPreamble(false);

  private String compile(String code) {
    var output = new StringBuilder();
    var codegen = new CCodeGenerator(codegenParamsBuilder.build(), output);
    var compilerParams = CompilerParams.builder().build();
    var compiler = new Compiler(compilerParams, errorConsumer, codegen);
    assertTrue(compiler.compile(List.of(new SourceCode(code))));
    assertEquals(0, errorConsumer.errorCount());
    return output.toString();
  }

  @Test
  void testGenerateStructType() {
    var code =
        """
        module test;
        type struct Point {
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
          const char* name;
        };

        """,
        output);
  }

  @Test
  void testGenerateStructTypeWithoutModuleNameMangle() {
    var code =
        """
        module test;
        type struct Point {
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
  void testGenerateStructTypeWithSelfPointer() {
    var code =
        """
        module test;
        type struct Node {
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
        struct test__Color;

        struct test__Color {
          int64_t tag;
        };

        """,
        output);
  }

  @Test
  void testGenerateEnumType() {
    var code =
        """
        module test;
        type enum Color(int) {
          Red;
          Green(3);
          Blue;
        }
        """;
    var output = compile(code);
    assertEquals(
        """
        enum test__Color {
          test__Color_Red = 0,
          test__Color_Green = 3,
          test__Color_Blue = 4,
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
        struct test__Maybe;

        struct test__Maybe {
          int64_t tag;
          union {
            int64_t Some;
            const char* Error;
          } u;
        };

        """,
        output);
  }

  @Test
  void testGenerateTypeDefinitionsInCOrder() {
    var code =
        """
        module test;
        type struct Node {
          var next Maybe*;
        }
        type union Maybe {
          None;
          Some(Node*);
        }
        type enum Status {
          Ok;
          Failed(2);
        }
        """;
    var output = compile(code);
    assertEquals(
        """
        struct test__Node;
        struct test__Maybe;

        enum test__Status {
          test__Status_Ok = 0,
          test__Status_Failed = 2,
        };

        struct test__Node {
          struct test__Maybe* next;
        };

        struct test__Maybe {
          int64_t tag;
          union {
            struct test__Node* Some;
          } u;
        };

        """,
        output);
  }
}
