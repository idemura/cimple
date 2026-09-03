package io.lang.cimple.compiler;

import java.util.function.Function;

public final class AstArrayType extends AstType {
  private AstType baseType;

  public AstArrayType(AstType baseType) {
    this.baseType = baseType;
  }

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {
    baseType.accept(visitor);
  }

  @Override
  public int hashCode() {
    return baseType.hashCode();
  }

  @Override
  public boolean equals(Object object) {
    return this == object
        || (object instanceof AstArrayType other && baseType.equals(other.baseType));
  }

  @Override
  public Identifier name() {
    var baseName = baseType.name();
    return baseName.copy().entity(baseName.entity() + "[]");
  }

  public AstType baseType() {
    return baseType;
  }

  public void resolve(Function<AstType, AstType> function) {
    this.baseType = function.apply(baseType);
  }
}
