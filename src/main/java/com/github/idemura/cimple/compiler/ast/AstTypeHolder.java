package com.github.idemura.cimple.compiler.ast;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Objects;

// Marks the ownership boundary for a type tree. Type resolution replaces the holder type, so
// declarations and expressions do not need custom code for each type field.
public class AstTypeHolder extends AstHolder {
  private AstType value;

  public static AstTypeHolder of(AstType type) {
    return type == null ? null : new AstTypeHolder(type);
  }

  public AstTypeHolder(AstType value) {
    this.value = checkNotNull(value);
  }

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
    value.accept(visitor);
  }

  public AstType value() {
    return value;
  }

  public void value(AstType type) {
    this.value = checkNotNull(type);
  }
}
