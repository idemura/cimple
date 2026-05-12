package com.github.idemura.cimple.compiler.ast;

public final class AstArrayAccess extends AstExpression {
  private AstExpression array;
  private AstExpression index;

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {
    array.accept(visitor);
    index.accept(visitor);
  }

  @Override
  public AstExpression rewrite(AstExpressionRewriter rewriter) {
    array = array.rewrite(rewriter);
    index = index.rewrite(rewriter);
    return rewriter.rewrite(this);
  }

  @Override
  public AstType type() {
    throw new UnsupportedOperationException();
  }

  public AstExpression array() {
    return array;
  }

  public void array(AstExpression array) {
    this.array = array;
  }

  public AstExpression index() {
    return index;
  }

  public void index(AstExpression index) {
    this.index = index;
  }
}
