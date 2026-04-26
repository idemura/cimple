package com.github.idemura.cimple.compiler.ast;

import java.util.Objects;

public abstract class AstLiteral extends AstExpression {
  private final Object value;

  protected AstLiteral(Object value) {
    this.value = value;
  }

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getClass(), value);
  }

  @Override
  public boolean equals(Object object) {
    return this == object
        || (object != null
            && getClass() == object.getClass()
            && Objects.equals(value, ((AstLiteral) object).value));
  }

  public Object getValue() {
    return value;
  }
}
