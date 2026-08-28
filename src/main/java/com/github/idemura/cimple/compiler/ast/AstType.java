package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.Identifier;

public abstract sealed class AstType extends AstNode
    permits AstTypeRef,
        AstPointerType,
        AstArrayType,
        AstBuiltinType,
        AstStringType,
        AstFunctionType,
        AstRecordType,
        AstUnionType,
        AstEnumType {
  protected AstType() {}

  public abstract Identifier name();

  public abstract void name(Identifier name);

  public String formatName() {
    var name = name();
    if (name.isBuiltin()) {
      return name.typeName();
    }
    return name.toString();
  }

  @Override
  public int hashCode() {
    return name().hashCode();
  }

  @Override
  public boolean equals(Object object) {
    if (object == null) {
      return false;
    }
    if (object == this) {
      return true;
    }
    if (object.getClass() != getClass()) {
      return false;
    }
    return name().equals(((AstType) object).name());
  }

  @Override
  public String toString() {
    return name().toString();
  }
}
