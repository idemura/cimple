package com.github.idemura.cimple.compiler.ast;

public final class AstCompoundAssign extends AstExpression {
  private AstExpression target;
  private AstEntityRef operation;
  private AstExpression value;

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {
    target.accept(visitor);
    operation.accept(visitor);
    value.accept(visitor);
  }

  @Override
  public AstExpression rewrite(AstExpressionRewriteVisitor visitor) {
    target = target.rewrite(visitor);
    value = value.rewrite(visitor);
    return visitor.rewrite(this);
  }

  @Override
  public AstType type() {
    return target.type();
  }

  public AstExpression target() {
    return target;
  }

  public void target(AstExpression target) {
    this.target = target;
  }

  public AstEntityRef operation() {
    return operation;
  }

  public void operation(AstEntityRef operation) {
    this.operation = operation;
  }

  public AstExpression value() {
    return value;
  }

  public void value(AstExpression value) {
    this.value = value;
  }
}
