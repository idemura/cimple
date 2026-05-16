package com.github.idemura.cimple.compiler.ast;

public final class AstAssign extends AstExpression {
  private AstExpression target;
  private AstExpression value;

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {
    target.accept(visitor);
    value.accept(visitor);
  }

  @Override
  public AstExpression rewrite(AstExpressionRewriter rewriter) {
    target = target.rewrite(rewriter);
    value = value.rewrite(rewriter);
    return rewriter.rewrite(this);
  }

  @Override
  public AstType type() {
    return value.type();
  }

  public AstExpression target() {
    return target;
  }

  public void target(AstExpression target) {
    this.target = target;
  }

  public AstExpression value() {
    return value;
  }

  public void value(AstExpression value) {
    this.value = value;
  }
}
