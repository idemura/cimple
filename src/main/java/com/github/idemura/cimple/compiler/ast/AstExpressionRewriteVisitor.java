package com.github.idemura.cimple.compiler.ast;

public class AstExpressionRewriteVisitor extends AstVisitor {
  private final AstExpressionRewriter rewriter;

  public AstExpressionRewriteVisitor(AstExpressionRewriter rewriter) {
    this.rewriter = rewriter;
  }

  @Override
  protected Object visit(AstExpressionHolder node) {
    node.root(node.root().acceptRewriter(rewriter));
    return node;
  }
}
