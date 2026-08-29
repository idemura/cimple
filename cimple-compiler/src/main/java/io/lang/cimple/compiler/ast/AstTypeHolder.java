package io.lang.cimple.compiler.ast;

import java.util.Objects;

// Marks the ownership boundary for a type tree. Type resolution replaces the holder type, so
// declarations and expressions do not need custom code for each type field.
public final class AstTypeHolder extends AstHolder {
  private AstType value;

  public AstTypeHolder() {}

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @Override
  public boolean equals(Object object) {
    return this == object
        || (object instanceof AstTypeHolder other && Objects.equals(value, other.value));
  }

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {
    if (value != null) {
      value.accept(visitor);
    }
  }

  public AstType get() {
    return value;
  }

  public void set(AstType type) {
    this.value = type;
  }
}
