package io.lang.cimple.compiler.ast;

import io.lang.cimple.compiler.Identifier;
import java.util.List;

public final class AstUtils {
  private AstUtils() {}

  public static AstTypeRef newTypeRef(String name) {
    return newTypeRef(null, name);
  }

  public static AstTypeRef newTypeRef(String moduleName, String name) {
    var ref = new AstTypeRef();
    ref.name(Identifier.ofType(name).module(moduleName));
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

  public static AstArrayType arrayType(AstType baseType) {
    return new AstArrayType(baseType);
  }

  public static AstVariableRef newVariableRef(String name) {
    return newVariableRef(null, name);
  }

  public static AstVariableRef newVariableRef(String moduleName, String name) {
    return new AstVariableRef(null, Identifier.of(name).module(moduleName));
  }

  public static AstFunctionRef newFunctionRef(String name) {
    return newFunctionRef(null, name);
  }

  public static AstFunctionRef newFunctionRef(String moduleName, String name) {
    return new AstFunctionRef(null, Identifier.of(name).module(moduleName));
  }

  public static AstFunctionRef newBuiltinFunctionRef(String name) {
    return new AstFunctionRef(null, Identifier.of(name).builtin());
  }

  public static AstStructType newStructType(String moduleName, String name) {
    var type = new AstStructType();
    type.name(Identifier.ofType(name).module(moduleName));
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
    return ((AstReturn) function.block().statements().get(0)).expression().get();
  }

  public static AstFunction function(String name) {
    var header = new AstFunctionHeader();
    header.parameters(List.of());
    var function = new AstFunction();
    function.name(Identifier.of(name));
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
    variable.name(Identifier.of(name).module(moduleName));
    if (flags != 0) {
      variable.setBit(flags);
    }
    variable.type(type);
    return variable;
  }
}
