package io.lang.cimple.compiler;

public final class AstCast extends AstExpression {
  private AstExpression expression;
  private final AstTypeHolder type;

  public AstCast(Location location, AstExpression expression, AstType type) {
    super(location);
    this.expression = expression;
    this.type = new AstTypeHolder(type);
  }

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {
    expression.accept(visitor);
    acceptSafe(type, visitor);
  }

  @Override
  public AstExpression rewrite(AstExpressionRewriteVisitor visitor) {
    expression = expression.rewrite(visitor);
    return visitor.rewrite(this);
  }

  @Override
  public AstType type() {
    return type.get();
  }

  public void type(AstType type) {
    this.type.set(type);
  }

  public AstExpression expression() {
    return expression;
  }
}
