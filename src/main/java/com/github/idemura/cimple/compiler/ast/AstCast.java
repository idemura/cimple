package com.github.idemura.cimple.compiler.ast;

public final class AstCast extends AstExpression {
  private AstExpression expression;
  private final AstTypeHolder type = new AstTypeHolder();

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {
    expression.accept(visitor);
    acceptSafe(type, visitor);
  }

  @Override
  public AstExpression rewrite(AstExpressionRewriter rewriter) {
    expression = expression.rewrite(rewriter);
    return rewriter.rewrite(this);
  }

  @Override
  public AstType type() {
    return type.get();
  }

  public void type(AstType type) {
    this.type.set(type);
  }

  public AstExpression expression() {
    return expression;
  }

  public void expression(AstExpression expression) {
    this.expression = expression;
  }
}
