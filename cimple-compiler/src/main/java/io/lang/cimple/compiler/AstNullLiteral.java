package io.lang.cimple.compiler;

public final class AstNullLiteral extends AstLiteral {
  public AstNullLiteral(Location location) {
    super(location, null);
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
