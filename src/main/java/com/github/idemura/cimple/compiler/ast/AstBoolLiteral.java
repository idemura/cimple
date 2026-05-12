package com.github.idemura.cimple.compiler.ast;

public final class AstBoolLiteral extends AstLiteral {
  public AstBoolLiteral(boolean value) {
    super(value);
  }

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  @Override
  public AstExpression acceptRewriter(AstExpressionRewriter rewriter) {
    return rewriter.rewrite(this);
  }
}
