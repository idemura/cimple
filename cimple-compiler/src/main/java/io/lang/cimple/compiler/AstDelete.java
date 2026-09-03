package io.lang.cimple.compiler;

public final class AstDelete extends AstStatement {
  private final AstExpressionHolder expression;

  public AstDelete(Location location, AstExpression expression) {
    super(location);
    this.expression = new AstExpressionHolder(expression);
  }

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {
    acceptSafe(expression, visitor);
  }

  public AstExpression expression() {
    return expression.get();
  }

  public void expression(AstExpression expression) {
    this.expression.set(expression);
  }
}
