package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.Location;
import java.util.Objects;

public abstract sealed class AstLiteral extends AstExpression
    permits AstBoolLiteral, AstNullLiteral, AstNumberLiteral, AstStringLiteral {
  private final Object value;
  private AstTypeHolder type;
  private Location location;

  protected AstLiteral(Object value) {
    this.value = value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(getClass(), value);
  }

  @Override
  public boolean equals(Object object) {
    return this == object
        || (object instanceof AstLiteral other
            && Objects.equals(value, other.value)
            && Objects.equals(type(), other.type()));
  }

  public Object value() {
    return value;
  }

  @Override
  public AstType type() {
    return type == null ? null : type.value();
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {
    acceptSafe(type, visitor);
  }

  public void type(AstType type) {
    this.type = AstTypeHolder.of(type);
  }

  public AstTypeHolder typeHolder() {
    return type;
  }

  @Override
  public Location location() {
    return location;
  }

  @Override
  public void location(Location location) {
    this.location = location;
  }
}
