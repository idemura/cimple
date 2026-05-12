package com.github.idemura.cimple.compiler.ast;

public final class AstCast extends AstExpression {
  private AstExpression expression;
  private AstType type;

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public AstExpression acceptRewriter(AstExpressionRewriter rewriter) {
    expression = expression.acceptRewriter(rewriter);
    return rewriter.rewrite(this);
  }

  @Override
  public AstType type() {
    return type;
  }

  public void type(AstType type) {
    this.type = type;
  }

  public AstExpression expression() {
    return expression;
  }

  public void expression(AstExpression expression) {
    this.expression = expression;
  }
}
