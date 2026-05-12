package com.github.idemura.cimple.compiler.ast;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Objects;

// Marks the ownership boundary for a type tree. Type resolution replaces the holder type, so
// declarations and expressions do not need custom code for each type field.
public class AstTypeHolder extends AstHolder {
  private AstType type;

  public static AstTypeHolder of(AstType type) {
    return type == null ? null : new AstTypeHolder(type);
  }

  public AstTypeHolder(AstType type) {
    this.type = checkNotNull(type);
  }

  @Override
  public int hashCode() {
    return type.hashCode();
  }

  @Override
  public boolean equals(Object object) {
    return this == object || (object instanceof AstTypeHolder other && Objects.equals(type, other.type));
  }

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  public AstType type() {
    return type;
  }

  public void type(AstType type) {
    this.type = checkNotNull(type);
  }
}
