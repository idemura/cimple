package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.Location;
import java.util.Objects;

public abstract sealed class AstLiteral extends AstExpression
    permits AstBoolLiteral, AstNullLiteral, AstNumberLiteral, AstStringLiteral {
  private final Object value;
  private AstTypeRef typeRef;
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
            && Objects.equals(typeRef, other.typeRef));
  }

  public Object value() {
    return value;
  }

  public AstTypeRef typeRef() {
    return typeRef;
  }

  public void typeRef(AstTypeRef typeRef) {
    this.typeRef = typeRef;
  }

  public Location location() {
    return location;
  }

  public void location(Location location) {
    this.location = location;
  }
}
