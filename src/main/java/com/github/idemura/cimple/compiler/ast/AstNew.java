package com.github.idemura.cimple.compiler.ast;

public final class AstNew extends AstExpression {
  private AstTypeHolder type;
  private AstExpression size;

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {
    acceptSafe(type, visitor);
    acceptSafe(size, visitor);
  }

  @Override
  public AstExpression rewrite(AstExpressionRewriter rewriter) {
    if (size != null) {
      size = size.rewrite(rewriter);
    }
    return rewriter.rewrite(this);
  }

  @Override
  public AstType type() {
    return type == null ? null : type.type();
  }

  public void type(AstType type) {
    this.type = AstTypeHolder.of(type);
  }

  public AstTypeHolder typeHolder() {
    return type;
  }

  public AstExpression size() {
    return size;
  }

  public void size(AstExpression size) {
    this.size = size;
  }
}
