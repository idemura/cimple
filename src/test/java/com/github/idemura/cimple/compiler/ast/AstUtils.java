package com.github.idemura.cimple.compiler.ast;

import static com.github.idemura.cimple.compiler.Constants.BUILTIN_MODULE;

import com.github.idemura.cimple.compiler.Identifier;

public final class AstUtils {
  private AstUtils() {}

  public static AstEntityRef builtinEntityRef(String symbol) {
    return AstEntityRef.ofName(BUILTIN_MODULE, symbol);
  }

  public static AstBoolLiteral boolLiteral(boolean value) {
    var literal = new AstBoolLiteral(value);
    literal.typeRef(AstTypeRef.ofType(AstBuiltinType.BOOL));
    return literal;
  }

  public static AstNullLiteral nullLiteral() {
    var literal = new AstNullLiteral();
    literal.typeRef(AstTypeRef.ofType(AstBuiltinType.NULL));
    return literal;
  }

  public static AstExpression extractReturnExpression(AstFunction function) {
    return ((AstReturn) function.block().statements().get(0)).expression();
  }

  public static AstFunction function(String name) {
    return function(null, name);
  }

  public static AstFunction function(String receiverTypeName, String name) {
    var header = new AstFunctionHeader();
    if (receiverTypeName != null) {
      header.receiverType(AstTypeRef.ofName(receiverTypeName));
    }
    var function = new AstFunction();
    function.name(Identifier.ofTypeEntity(receiverTypeName, name));
    function.header(header);
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
    unionVariant.tag(name);
    if (typeName != null) {
      unionVariant.valueType(AstTypeRef.ofName(typeName));
    }
    return unionVariant;
  }

  private static AstVariable variable(
      String moduleName, String name, long flags, AstTypeRef typeRef) {
    var variable = new AstVariable();
    variable.name(Identifier.ofEntity(name).withModule(moduleName));
    if (flags != 0) {
      variable.setBit(flags);
    }
    variable.typeRef(typeRef);
    return variable;
  }
}
