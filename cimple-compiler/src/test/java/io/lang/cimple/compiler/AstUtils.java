package io.lang.cimple.compiler;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;

final class AstUtils {
  AstUtils() {}

  static void analyze(String code, ErrorConsumer errorConsumer) {
    var module = Parser.parseCode(code, errorConsumer);
    new SemanticAnalyzer(errorConsumer).analyze(List.of(module));
  }

  static AstTypeRef newTypeRef(String name) {
    return newTypeRef(null, name);
  }

  static AstTypeRef newTypeRef(String moduleName, String name) {
    return new AstTypeRef(identifier(moduleName, name));
  }

  static AstTypeRef newBuiltinTypeRef(String name) {
    return new AstTypeRef(new Identifier(name).builtin());
  }

  static AstVariableRef newVariableRef(String name) {
    return newVariableRef(null, name);
  }

  static AstVariableRef newVariableRef(String moduleName, String name) {
    return new AstVariableRef(identifier(moduleName, name));
  }

  static AstFunctionRef newFunctionRef(String name) {
    return newFunctionRef(null, name);
  }

  static AstFunctionRef newFunctionRef(String moduleName, String name) {
    return new AstFunctionRef(identifier(moduleName, name));
  }

  static AstFunctionRef newBuiltinFunctionRef(String name) {
    return new AstFunctionRef(new Identifier(name).builtin());
  }

  static AstStructType newStructType(String moduleName, String name) {
    return new AstStructType(identifier(moduleName, name), ImmutableList.of());
  }

  static AstBoolLiteral boolLiteral(boolean value) {
    return new AstBoolLiteral(null, value);
  }

  static AstNullLiteral nullLiteral() {
    return new AstNullLiteral(null);
  }

  static AstExpression extractReturnExpression(AstFunction function) {
    return ((AstReturn) function.block().statements().get(0)).expression();
  }

  static AstFunction function(String name) {
    return new AstFunction(new Identifier(name), ImmutableList.of(), null, null);
  }

  static AstFunction function(String name, AstType... parameterTypes) {
    return function("test", name, parameterTypes);
  }

  static AstFunction function(String moduleName, String name, AstType... parameterTypes) {
    return new AstFunction(
        entityName(moduleName, name), ImmutableList.copyOf(parameters(parameterTypes)), null, null);
  }

  static AstFunction freeFunction(String moduleName, String name, AstType... parameterTypes) {
    return function(moduleName, name, parameterTypes);
  }

  static List<AstVariable> parameters(AstType... parameterTypes) {
    var parameters = new ArrayList<AstVariable>();
    for (var i = 0; i < parameterTypes.length; i++) {
      var parameter = parameter("p" + i);
      parameter.type(parameterTypes[i]);
      parameters.add(parameter);
    }
    return parameters;
  }

  static AstVariable rawVariable(String name, String typeName) {
    return variable(null, name, 0, newTypeRef(typeName));
  }

  static AstVariable rawVariable(String name) {
    return variable(null, name, 0, null);
  }

  static AstVariable globalVariable(String moduleName, String name) {
    return variable(moduleName, name, AstVariable.GLOBAL, null);
  }

  static AstVariable localVariable(String name) {
    return variable(null, name, AstVariable.LOCAL, null);
  }

  static AstVariable parameter(String name) {
    return variable(null, name, AstVariable.PARAMETER, null);
  }

  static AstUnionVariant unionVariant(String name, String typeName) {
    return new AstUnionVariant(
        new Identifier(name), typeName == null ? null : newTypeRef(typeName));
  }

  static Identifier typeName(String moduleName, String name) {
    return entityName(moduleName, name);
  }

  static Identifier entityName(String moduleName, String name) {
    return new Identifier(name).module(moduleName);
  }

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

  static AstEnumType enumType(AstModule module, String name) {
    return (AstEnumType) module.findType(name);
  }

  static AstVariable variable(String moduleName, String name, long flags, AstType type) {
    var variable = new AstVariable(identifier(moduleName, name), type, null);
    variable.type(type);
    variable.flags(flags);
    return variable;
  }

  static Identifier identifier(String moduleName, String name) {
    var identifier = new Identifier(name);
    if (moduleName != null) {
      identifier.module(moduleName);
    }
    return identifier;
  }
}
