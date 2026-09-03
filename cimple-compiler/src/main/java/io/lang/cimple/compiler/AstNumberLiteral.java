package io.lang.cimple.compiler;

public final class AstNumberLiteral extends AstLiteral {
  // Test helper.
  public static AstNumberLiteral of(long value) {
    return new AstNumberLiteral(null, Long.toString(value));
  }

  public AstNumberLiteral(Location location, Object value) {
    super(location, value);
    // The exact numeric type is assigned later during semantic analysis.
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
  public AstExpression rewrite(AstExpressionRewriteVisitor visitor) {
    return visitor.rewrite(this);
  }
}
