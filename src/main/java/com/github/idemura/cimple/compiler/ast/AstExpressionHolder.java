package com.github.idemura.cimple.compiler.ast;

import static com.google.common.base.Preconditions.checkNotNull;

// Marks the ownership boundary for an expression tree. Rewrites replace the holder root, so
// statements and declarations do not need custom code for each expression field.
public final class AstExpressionHolder extends AstHolder {
  private AstExpression value;

  public static AstExpressionHolder of(AstExpression root) {
    return root == null ? null : new AstExpressionHolder(root);
  }

  public AstExpressionHolder(AstExpression value) {
    this.value = checkNotNull(value);
  }

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {
    value.accept(visitor);
  }

  public AstExpression value() {
    return value;
  }

  public void value(AstExpression expression) {
    this.value = checkNotNull(expression);
  }

  public AstType type() {
    return value.type();
  }
}
