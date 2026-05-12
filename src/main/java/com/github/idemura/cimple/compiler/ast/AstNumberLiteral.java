package com.github.idemura.cimple.compiler.ast;

public final class AstNumberLiteral extends AstLiteral {
  // Test helper.
  public static AstNumberLiteral of(long value) {
    return new AstNumberLiteral(Long.toString(value));
  }

  public AstNumberLiteral(Object value) {
    super(value);
    // The exact numeric type is assigned later during semantic analysis.
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
