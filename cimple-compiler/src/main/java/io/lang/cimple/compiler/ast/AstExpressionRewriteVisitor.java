package io.lang.cimple.compiler.ast;

import static com.google.common.base.Preconditions.checkState;

public class AstExpressionRewriteVisitor extends AstVisitor {
  private AstExpression expressionRoot;

  @Override
  protected void visit(AstExpressionHolder node) {
    if (node.get() != null) {
      node.set(rewriteExpression(node.get()));
    }
    // Do not visit children. Rewriter will do this.
  }

  public final AstExpression rewriteExpression(AstExpression expr) {
    checkState(expressionRoot == null);
    try {
      this.expressionRoot = expr;
      return expr.rewrite(this);
    } finally {
      this.expressionRoot = null;
    }
  }

  protected AstExpression expressionRoot() {
    return expressionRoot;
  }

  protected AstExpression rewrite(AstNullLiteral node) {
    return node;
  }

  protected AstExpression rewrite(AstBoolLiteral node) {
    return node;
  }

  protected AstExpression rewrite(AstNumberLiteral node) {
    return node;
  }

  protected AstExpression rewrite(AstStringLiteral node) {
    return node;
  }

  protected AstExpression rewrite(AstEntityRef node) {
    return node;
  }

  protected AstExpression rewrite(AstVariableRef node) {
    return rewrite((AstEntityRef) node);
  }

  protected AstExpression rewrite(AstFunctionRef node) {
    return rewrite((AstEntityRef) node);
  }

  protected AstExpression rewrite(AstAssign node) {
    return node;
  }

  protected AstExpression rewrite(AstCompoundAssign node) {
    return node;
  }

  protected AstExpression rewrite(AstNew node) {
    return node;
  }

  protected AstExpression rewrite(AstCall node) {
    return node;
  }

  protected AstExpression rewrite(AstFunctionPointerCall node) {
    return node;
  }

  protected AstExpression rewrite(AstArrayAccess node) {
    return node;
  }

  protected AstExpression rewrite(AstFieldAccess node) {
    return node;
  }

  protected AstExpression rewrite(AstCast node) {
    return node;
  }
}
