package com.github.idemura.cimple.compiler.ast;

public class AstExpressionRewriter {
  private AstExpression root;

  public final AstExpression rewriteRoot(AstExpression root) {
    var previous = this.root;
    try {
      this.root = root;
      return root.rewrite(this);
    } finally {
      this.root = previous;
    }
  }

  protected AstExpression root() {
    return root;
  }

  public AstExpression rewrite(AstNullLiteral node) {
    return node;
  }

  public AstExpression rewrite(AstBoolLiteral node) {
    return node;
  }

  public AstExpression rewrite(AstNumberLiteral node) {
    return node;
  }

  public AstExpression rewrite(AstStringLiteral node) {
    return node;
  }

  public AstExpression rewrite(AstEntityRef node) {
    return node;
  }

  public AstExpression rewrite(AstAssign node) {
    return node;
  }

  public AstExpression rewrite(AstNew node) {
    return node;
  }

  public AstExpression rewrite(AstCall node) {
    return node;
  }

  public AstExpression rewrite(AstArrayAccess node) {
    return node;
  }

  public AstExpression rewrite(AstFieldAccess node) {
    return node;
  }

  public AstExpression rewrite(AstReceiverLookup node) {
    return node;
  }

  public AstExpression rewrite(AstCast node) {
    return node;
  }
}
