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

  protected AstType(Location location) {
    super(location);
  }

  public abstract Identifier name();

  @Override
  public int hashCode() {
    return name().hashCode();
  }

  @Override
  public boolean equals(Object object) {
    return this == object
        || (object instanceof AstType other && Objects.equals(name(), other.name()));
  }

  @Override
  public String toString() {
    var name = name();
    if (name.isBuiltin()) {
      return name.entity();
    }
    return name.toString();
  }
}
