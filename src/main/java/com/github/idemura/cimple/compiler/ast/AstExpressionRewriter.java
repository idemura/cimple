package com.github.idemura.cimple.compiler.ast;

public class AstExpressionRewriter {
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
