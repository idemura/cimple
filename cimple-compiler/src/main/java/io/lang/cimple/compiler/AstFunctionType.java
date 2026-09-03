package io.lang.cimple.compiler;

import java.util.Objects;

public final class AstFunctionType extends AstType {
  private final AstFunction function;

  public AstFunctionType(AstFunction function) {
    this.function = function;
  }

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {
    function.accept(visitor);
  }

  @Override
  public int hashCode() {
    return function.hashCode();
  }

  @Override
  public boolean equals(Object object) {
    return this == object
        || (object instanceof AstFunctionType other && Objects.equals(function, other.function));
  }

  @Override
  public Identifier name() {
    return function.name();
  }

  public AstFunction function() {
    return function;
  }
}
