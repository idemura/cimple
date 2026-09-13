package io.lang.cimple.compiler;

import java.util.Objects;

public abstract sealed class AstLiteral extends AstExpression
    permits AstBoolLiteral, AstNullLiteral, AstNumberLiteral, AstStringLiteral {
  private final Object value;
  private final AstTypeHolder type;

  protected AstLiteral(Location location, Object value, AstType type) {
    super(location);
    this.value = value;
    this.type = new AstTypeHolder(type);
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
    return type.get();
  }

  public void type(AstType type) {
    this.type.set(type);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {
    acceptSafe(type, visitor);
  }
}
