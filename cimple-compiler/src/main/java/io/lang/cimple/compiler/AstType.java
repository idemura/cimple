package io.lang.cimple.compiler;

import java.util.Objects;

public abstract sealed class AstType extends AstNode
    permits AstTypeRef,
        AstPointerType,
        AstArrayType,
        AstBuiltinType,
        AstStringType,
        AstFunctionType,
        AstInterfaceType,
        AstStructType,
        AstUnionType,
        AstEnumType {
  protected AstType() {}

  public abstract Identifier name();

  public String formatName() {
    var name = name();
    if (name.isBuiltin()) {
      return name.entity();
    }
    return name.toString();
  }

  @Override
  public int hashCode() {
    return name().hashCode();
  }

  @Override
  public boolean equals(Object object) {
    return this == object
        || (object instanceof AstFunctionType other && Objects.equals(name(), other.name()));
  }

  @Override
  public String toString() {
    return name().toString();
  }
}
