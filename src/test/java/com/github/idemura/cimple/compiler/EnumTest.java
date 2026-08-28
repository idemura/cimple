package com.github.idemura.cimple.compiler;

import static org.junit.jupiter.api.Assertions.*;

import com.github.idemura.cimple.compiler.ast.AstBuiltinType;
import com.github.idemura.cimple.compiler.ast.AstEnumType;
import com.github.idemura.cimple.compiler.ast.AstModule;
import com.github.idemura.cimple.compiler.ast.AstNumberLiteral;
import java.util.List;
import org.junit.jupiter.api.Test;

class EnumTest extends AbstractSemanticsTest {
  private static AstEnumType enumType(AstModule module, String name) {
    return (AstEnumType) module.findType(name);
  }

  private static void assertVariantValue(AstEnumType enumType, int index, String name, long value) {
    var variant = enumType.variants().get(index);
    assertEquals(name, variant.tag());
    assertEquals(value, variant.value());
  }

  @Test
  void testEnumValueAssignment() {
    var code =
        """
        module test;
        type enum Color(int) {
          Red;
          Green(3);
          Blue;
          Yellow(6);
        }
        """;
    var module = parseCode(code);
    new SemanticAnalyzer(errorConsumer).analyze(List.of(module));
    assertEquals(List.of(), errorConsumer.errors());

    var type = enumType(module, "Color");
    assertEquals(AstBuiltinType.INT64, type.baseType());
    assertVariantValue(type, 0, "Red", 0);
    assertVariantValue(type, 1, "Green", 3);
    assertVariantValue(type, 2, "Blue", 4);
    assertVariantValue(type, 3, "Yellow", 6);
  }

  @Test
  void testEnumDefaultBaseType() {
    var code =
        """
        module test;
        type enum Color {
          Red;
        }
        """;
    var module = parseCode(code);
    new SemanticAnalyzer(errorConsumer).analyze(List.of(module));
    assertEquals(List.of(), errorConsumer.errors());
    assertEquals(AstBuiltinType.INT64, enumType(module, "Color").baseType());
  }

  @Test
  void testDuplicateEnumVariantFailure() {
    var code =
        """
        module test;
        type enum E(int64) {
          A;
          A(1);
        }
        """;
    var module = parseCode(code);
    new SemanticAnalyzer(errorConsumer).analyze(List.of(module));
    assertEquals(
        List.of("Duplicate enum variant 'A'. First defined at 3,3."), errorConsumer.errors());
  }

  @Test
  void testDuplicateEnumValues() {
    var code =
        """
        module test;
        type enum E(int64) {
          A;
          B(0);
          C;
        }
        """;
    var module = parseCode(code);
    new SemanticAnalyzer(errorConsumer).analyze(List.of(module));
    assertEquals(List.of(), errorConsumer.errors());

    var type = enumType(module, "E");
    assertVariantValue(type, 0, "A", 0);
    assertVariantValue(type, 1, "B", 0);
    assertVariantValue(type, 2, "C", 1);
  }

  @Test
  void testEnumRequiresZeroValue() {
    var code =
        """
        module test;
        type enum E(int64) {
          A(1);
          B;
        }
        """;
    var module = parseCode(code);
    new SemanticAnalyzer(errorConsumer).analyze(List.of(module));
    assertEquals(
        List.of("Enum 'test~E' must define a variant with value 0"), errorConsumer.errors());
  }

  @Test
  void testEnumRequiresIntegerBaseType() {
    var code =
        """
        module test;
        type enum E(float64) {
          A;
        }
        """;
    var module = parseCode(code);
    new SemanticAnalyzer(errorConsumer).analyze(List.of(module));
    assertEquals(
        List.of("Enum 'test~E' base type must be an integer type, got 'float64'"),
        errorConsumer.errors());
  }

  @Test
  void testEnumRequiresNumberLiteralValue() {
    var code =
        """
        module test;
        type enum E(int64) {
          A(4 + 2);
        }
        """;
    var module = parseCode(code);
    new SemanticAnalyzer(errorConsumer).analyze(List.of(module));
    assertEquals(
        List.of("Enum variant 'A' value must be a number literal"), errorConsumer.errors());
  }

  @Test
  void testEnumRequiresIntegerValueType() {
    var code =
        """
        module test;
        type enum E(int64) {
          A(1);
        }
        """;
    var module = parseCode(code);
    var literal = new AstNumberLiteral(1.5);
    literal.type(AstBuiltinType.FLOAT64);
    enumType(module, "E").variants().get(0).valueExpression(literal);

    new SemanticAnalyzer(errorConsumer).analyze(List.of(module));
    assertEquals(
        List.of("Enum variant 'A' value has type 'float64', expected integer"),
        errorConsumer.errors());
  }

  @Test
  void testEnumVariantsDoNotLeakIntoValueNamespace() {
    var code =
        """
        module test;
        type enum Color(int64) {
          Red;
        }
        function f() int64 {
          return Red;
        }
        """;
    var module = parseCode(code);
    new SemanticAnalyzer(errorConsumer).analyze(List.of(module));
    assertEquals(List.of("Undefined name: 'Red'"), errorConsumer.errors());
  }
}
