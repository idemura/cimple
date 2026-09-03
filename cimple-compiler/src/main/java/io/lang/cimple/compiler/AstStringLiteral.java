package io.lang.cimple.compiler;

public final class AstStringLiteral extends AstLiteral {
  public AstStringLiteral(Location location, String value) {
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
