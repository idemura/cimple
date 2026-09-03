package io.lang.cimple.compiler;

public final class AstBoolLiteral extends AstLiteral {
  public AstBoolLiteral(Location location, boolean value) {
    super(location, value);
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
