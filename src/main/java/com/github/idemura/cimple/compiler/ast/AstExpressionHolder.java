package com.github.idemura.cimple.compiler.ast;

// Marks the ownership boundary for an expression tree. Rewrites replace the holder root, so
// statements and declarations do not need custom code for each expression field. Optional
// expression slots still use a holder with a null value so the ownership boundary is explicit.
public final class AstExpressionHolder extends AstHolder {
  private AstExpression value;

  public AstExpressionHolder(AstExpression value) {
    this.value = value;
  }

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {
    if (value != null) {
      value.accept(visitor);
    }
  }

  public AstExpression get() {
    return value;
  }

  public void set(AstExpression expression) {
    this.value = expression;
  }

  public AstType type() {
    return value.type();
  }
}
