package io.lang.cimple.compiler;

import static io.lang.cimple.compiler.AstTreeUtils.newTypeRef;
import static org.junit.jupiter.api.Assertions.*;

final class AstAssertionUtils {
  AstAssertionUtils() {}

  static void assertLookup(LocalNameMap localNameMap, AstVariable variable) {
    assertSame(variable, localNameMap.lookupVariable(variable.name().entity()));
  }

  static void assertEnumVariant(AstEnumVariant variant, String name, String valueExpression) {
    assertEquals(new Identifier(name), variant.tag());
    if (valueExpression == null) {
      assertNull(variant.expression());
    } else {
      var expression = (AstNumberLiteral) variant.expression();
      assertEquals(valueExpression, expression.value());
    }
  }

  static void assertVariantValue(AstEnumType enumType, int index, String name, long value) {
    var variant = enumType.variants().get(index);
    assertEquals(new Identifier(name), variant.tag());
    assertEquals(value, variant.value());
  }

  static void assertVariableSyntax(AstVariable variable, String name, String typeName, Long value) {
    assertEquals(new Identifier(name), variable.name());
    if (typeName == null) {
      assertNull(variable.type());
    } else {
      assertEquals(newTypeRef(typeName), variable.type());
    }
    if (value == null) {
      assertNull(variable.expression());
    } else {
      assertEquals(AstNumberLiteral.of(value), variable.expression());
    }
    assertTrue(variable.getBit(AstVariable.MUTABLE));
  }

  static void assertArrayAccess(AstFunction function, AstType expectedElementType) {
    var local = (AstLocal) function.block().statements().get(0);
    assertEquals(expectedElementType, local.variable().type());

    var access = (AstArrayAccess) local.variable().expression();
    assertEquals(expectedElementType, access.type());

    var array = (AstVariableRef) access.array();
    assertSame(function.parameters().get(0), array.variable());
    assertEquals(new AstArrayType(expectedElementType), array.type());
    assertEquals(AstBuiltinType.INT64, access.index().type());
  }

  static void assertOperator(AstStatement statement, AstFunction function) {
    var call = (AstCall) ((AstLocal) statement).variable().expression();
    var functionRef = call.function();
    assertSame(function, functionRef.function());
    assertEquals(AstBuiltinType.INT64, call.type());
  }

  static void assertComparisonOperator(AstStatement statement, AstFunction function) {
    var call = (AstCall) ((AstLocal) statement).variable().expression();
    var functionRef = call.function();
    assertSame(function, functionRef.function());
    assertEquals(AstBuiltinType.BOOL, call.type());
  }

  static void assertCompoundOperator(AstStatement statement, AstFunction function) {
    var expr = ((AstExpressionStatement) statement).expression();
    var assign = (AstCompoundAssign) expr;
    assertSame(function, assign.operation().function());
    assertEquals(AstBuiltinType.INT64, assign.type());
  }
}
