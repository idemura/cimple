package com.github.idemura.cimple.compiler.ast;

public final class AstNew extends AstExpression {
  private AstTypeHolder type;
  private AstExpression size;

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public AstExpression acceptRewriter(AstExpressionRewriter rewriter) {
    if (size != null) {
      size = size.acceptRewriter(rewriter);
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
