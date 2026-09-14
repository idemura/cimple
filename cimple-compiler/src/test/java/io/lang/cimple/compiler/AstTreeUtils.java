package io.lang.cimple.compiler;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;

final class AstTreeUtils {
  AstTreeUtils() {}

  static Identifier typeName(String moduleName, String name) {
    return entityName(moduleName, name);
  }

  static Identifier entityName(String moduleName, String name) {
    return new Identifier(name).module(moduleName);
  }

  static Identifier identifier(String moduleName, String name) {
    var identifier = new Identifier(name);
    if (moduleName != null) {
      identifier.module(moduleName);
    }
    return identifier;
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

  static AstTypeWildcard wildcard(String name) {
    return new AstTypeWildcard(new Identifier(name));
  }

  static AstStructType newStructType(String moduleName, String name) {
    return new AstStructType(identifier(moduleName, name), ImmutableList.of());
  }

  static AstUnionVariant unionVariant(String name, String typeName) {
    return new AstUnionVariant(
        new Identifier(name), typeName == null ? null : newTypeRef(typeName));
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

  static AstBoolLiteral boolLiteral(boolean value) {
    return new AstBoolLiteral(null, value);
  }

  static AstNullLiteral nullLiteral() {
    return new AstNullLiteral(null);
  }

  static AstFunction function(String name) {
    return new AstFunction(
        new Identifier(name), ImmutableList.of(), ImmutableList.of(), null, null);
  }

  static AstFunction function(String name, AstType... parameterTypes) {
    return function("test", name, parameterTypes);
  }

  static AstFunction function(String moduleName, String name, AstType... parameterTypes) {
    return new AstFunction(
        entityName(moduleName, name),
        ImmutableList.of(),
        ImmutableList.copyOf(parameters(parameterTypes)),
        null,
        null);
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

  static AstVariable variable(String moduleName, String name, long flags, AstType type) {
    var variable = new AstVariable(identifier(moduleName, name), type, null);
    variable.type(type);
    variable.flags(flags);
    return variable;
  }

  static AstExpression extractReturnExpression(AstFunction function) {
    return ((AstReturn) function.block().statements().get(0)).expression();
  }

  static AstEnumType enumType(AstModule module, String name) {
    return (AstEnumType) module.findType(name);
  }
}
