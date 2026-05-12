package com.github.idemura.cimple.compiler.ast;

public final class AstNullLiteral extends AstLiteral {
  public AstNullLiteral() {
    super(null);
  }

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {
    super.acceptChildren(visitor);
  }

  @Override
  public AstExpression rewrite(AstExpressionRewriter rewriter) {
    return rewriter.rewrite(this);
  }
}
