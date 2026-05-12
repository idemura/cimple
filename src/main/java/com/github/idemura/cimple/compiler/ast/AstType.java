package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.Identifier;

public abstract sealed class AstType extends AstNode
    permits AstTypeRef,
        AstPointerType,
        AstBuiltinType,
        AstFunctionType,
        AstRecordType,
        AstUnionType {
  protected AstType() {}

  public abstract Identifier name();

  public abstract void name(Identifier name);

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
    return "%s(%s)".formatted(getClass().getSimpleName(), name());
  }
}
