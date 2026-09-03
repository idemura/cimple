package io.lang.cimple.compiler;

public final class AstExpressionStatement extends AstStatement {
  private final AstExpressionHolder expression;

  public AstExpressionStatement(AstExpression expression) {
    super(expression.location());
    this.expression = new AstExpressionHolder(expression);
  }

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {
    expression.accept(visitor);
  }

  public AstExpression expression() {
    return expression.get();
  }

  public void expression(AstExpression expression) {
    this.expression.set(expression);
  }
}
