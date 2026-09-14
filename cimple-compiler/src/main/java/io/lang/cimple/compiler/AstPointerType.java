package io.lang.cimple.compiler;

import java.util.Map;
import java.util.function.Function;

public final class AstPointerType extends AstType {
  private AstType baseType;

  public AstPointerType(AstType baseType) {
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
        || (object instanceof AstPointerType other && baseType.equals(other.baseType));
  }

  @Override
  public Identifier name() {
    var baseName = baseType.name();
    return baseName.copy().entity(baseName.entity() + "*");
  }

  public AstType baseType() {
    return baseType;
  }

  @Override
  public boolean containsWildcard() {
    return baseType.containsWildcard();
  }

  @Override
  public AstType substitute(Map<AstTypeWildcard, AstType> substitutions) {
    if (containsWildcard()) {
      throw new UnsupportedOperationException("Generic pointer types are not supported yet");
    }
    return this;
  }

  public void resolve(Function<AstType, AstType> function) {
    this.baseType = function.apply(baseType);
  }
}
