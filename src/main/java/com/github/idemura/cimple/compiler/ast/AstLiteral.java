package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.Location;
import java.util.Objects;

public abstract sealed class AstLiteral extends AstExpression
    permits AstBoolLiteral, AstNullLiteral, AstNumberLiteral, AstStringLiteral {
  private final Object value;
  private AstTypeRef type;
  private Location location;

  protected AstLiteral(Object value) {
    this.value = value;
  }

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
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
            && Objects.equals(type, other.type));
  }

  public Object value() {
    return value;
  }

  public AstTypeRef getType() {
    return type;
  }

  public void setType(AstTypeRef type) {
    this.type = type;
  }

  public Location getLocation() {
    return location;
  }

  public void setLocation(Location location) {
    this.location = location;
  }
}
