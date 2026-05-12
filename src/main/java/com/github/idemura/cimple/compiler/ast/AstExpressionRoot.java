package com.github.idemura.cimple.compiler.ast;

import static com.google.common.base.Preconditions.checkNotNull;

public class AstExpressionRoot extends AstNode {
  private AstExpression root;

  public AstExpressionRoot(AstExpression root) {
    this.root = checkNotNull(root);
  }

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
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
