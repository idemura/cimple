package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.Identifier;

public final class AstUtils {
  private AstUtils() {}

  public static AstTypeRef newTypeRef(String name) {
    return newTypeRef(null, name);
  }

  public static AstTypeRef newTypeRef(String moduleName, String name) {
    var ref = new AstTypeRef();
    ref.name(Identifier.ofType(name).withModule(moduleName));
    return ref;
  }

  public static AstTypeRef newBuiltinTypeRef(String name) {
    var ref = new AstTypeRef();
    ref.name(Identifier.ofType(name).builtin());
    return ref;
  }

  public static AstPointerType pointerType(AstType baseType) {
    return new AstPointerType(baseType);
  }

  public static AstEntityRef newEntityRef(String name) {
    return newEntityRef(null, name);
  }

  public static AstEntityRef newEntityRef(String moduleName, String name) {
    var ref = new AstEntityRef();
    ref.name(Identifier.ofEntity(name).withModule(moduleName));
    return ref;
  }

  public static AstEntityRef newBuiltinEntityRef(String name) {
    var ref = new AstEntityRef();
    ref.name(Identifier.ofEntity(name).builtin());
    return ref;
  }

  public static AstRecordType newRecordType(String moduleName, String name) {
    var type = new AstRecordType();
    type.name(Identifier.ofType(name).withModule(moduleName));
    return type;
  }

  public static AstBoolLiteral boolLiteral(boolean value) {
    var literal = new AstBoolLiteral(value);
    literal.type(AstBuiltinType.BOOL);
    return literal;
  }

  public static AstNullLiteral nullLiteral() {
    var literal = new AstNullLiteral();
    literal.type(AstBuiltinType.NULL);
    return literal;
  }

  public static AstExpression extractReturnExpression(AstFunction function) {
    return ((AstReturn) function.block().statements().get(0)).expression().value();
  }

  public static AstFunction function(String name) {
    return function(null, name);
  }

  public static AstFunction function(String receiverTypeName, String name) {
    var header = new AstFunctionHeader();
    if (receiverTypeName != null) {
      header.receiverType(newTypeRef(receiverTypeName));
    }
    var function = new AstFunction();
    function.name(Identifier.ofTypeEntity(receiverTypeName, name));
    function.header(header);
    return function;
  }

  public static AstVariable rawVariable(String name, String typeName) {
    return variable(null, name, 0, newTypeRef(typeName));
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
    var variant = new AstUnionType.Variant();
    variant.tag(name);
    if (typeName != null) {
      variant.valueType(newTypeRef(typeName));
    }
    return variant;
  }

  private static AstVariable variable(String moduleName, String name, long flags, AstType type) {
    var variable = new AstVariable();
    variable.name(Identifier.ofEntity(name).withModule(moduleName));
    if (flags != 0) {
      variable.setBit(flags);
    }
    variable.type(type);
    return variable;
  }
}
