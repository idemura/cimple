package com.github.idemura.cimple.compiler.ast;

import static com.github.idemura.cimple.compiler.Constants.BUILTIN_MODULE;

import com.github.idemura.cimple.compiler.QualifiedName;

public final class AstUtils {
  private AstUtils() {}

  public static AstEntityRef builtinEntityRef(String symbol) {
    return AstEntityRef.ofName(BUILTIN_MODULE, symbol);
  }

  public static AstFunction function(String name) {
    return function(null, name);
  }

  public static AstFunction function(String receiverTypeName, String name) {
    var function = new AstFunction();
    var header = new AstFunctionHeader();
    header.setName(new QualifiedName(name));
    if (receiverTypeName != null) {
      header.setReceiverType(AstTypeRef.ofName(receiverTypeName));
    }
    function.setHeader(header);
    return function;
  }

  public static AstVariable rawVariable(String name, String typeName) {
    return variable(null, name, 0, AstTypeRef.ofName(typeName));
  }

  public static AstVariable rawVariable(String name) {
    return variable(null, name, 0, null);
  }

  public static AstVariable globalVariable(String moduleName, String name) {
    return variable(moduleName, name, AstVariable.GLOBAL, null);
  }

  public static AstVariable localVariable(String name) {
    return variable(null, name, AstVariable.LOCAL, null);
  }

  public static AstVariable parameter(String name) {
    return variable(null, name, AstVariable.PARAMETER, null);
  }

  public static AstUnionType.Variant unionVariant(String name, String typeName) {
    var unionVariant = new AstUnionType.Variant();
    unionVariant.setTag(name);
    if (typeName != null) {
      unionVariant.setValueType(AstTypeRef.ofName(typeName));
    }
    return unionVariant;
  }

  private static AstVariable variable(String moduleName, String name, long flags, AstTypeRef type) {
    var variable = new AstVariable();
    variable.setName(new QualifiedName(moduleName, name));
    if (flags != 0) {
      variable.setBit(flags);
    }
    variable.setType(type);
    return variable;
  }
}
