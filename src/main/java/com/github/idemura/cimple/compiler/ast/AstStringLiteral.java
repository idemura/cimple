package com.github.idemura.cimple.compiler.ast;

public final class AstStringLiteral extends AstLiteral {
  public AstStringLiteral(String value) {
    super(value);
  }

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public AstExpression acceptRewriter(AstExpressionRewriter rewriter) {
    return rewriter.rewrite(this);
  }
}
