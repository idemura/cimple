package com.github.idemura.cimple.compiler.ast;

import static com.google.common.base.Preconditions.checkNotNull;

// Marks the ownership boundary for an expression tree. Rewrites replace the holder root, so
// statements and declarations do not need custom code for each expression field.
public class AstExpressionHolder extends AstNode {
  private AstExpression root;

  public AstExpressionHolder(AstExpression root) {
    this.root = checkNotNull(root);
  }

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  public AstExpression root() {
    return root;
  }

  public void root(AstExpression expression) {
    this.root = checkNotNull(expression);
  }

  public AstType type() {
    return root.type();
  }
}
