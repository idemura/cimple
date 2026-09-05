package io.lang.cimple.compiler;

import java.util.Objects;

public final class AstEnumVariant extends AstNode {
  private final Identifier tag;
  private final AstExpressionHolder expression;
  private Long value; // Computed value of @expression.

  public AstEnumVariant(Identifier tag, AstExpression expression) {
    super(tag.location());
    this.tag = tag;
    this.expression = new AstExpressionHolder(expression);
  }

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {
    expression.accept(visitor);
  }

  @Override
  public int hashCode() {
    return tag.hashCode();
  }

  @Override
  public boolean equals(Object object) {
    return this == object
        || (object instanceof AstEnumVariant other && Objects.equals(tag, other.tag));
  }

  public Identifier tag() {
    return tag;
  }

  public AstExpression expression() {
    return expression.get();
  }

  void expression(AstExpression expression) {
    this.expression.set(expression);
  }

  public Long value() {
    return value;
  }

  public void value(long value) {
    this.value = value;
  }
}
