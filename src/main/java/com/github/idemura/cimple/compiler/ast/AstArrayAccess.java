package com.github.idemura.cimple.compiler.ast;

public final class AstArrayAccess extends AstExpression {
  private AstExpression array;
  private AstExpression index;

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  @Override
  public AstExpression acceptRewriter(AstExpressionRewriter rewriter) {
    array = array.acceptRewriter(rewriter);
    index = index.acceptRewriter(rewriter);
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
