package com.github.idemura.cimple.compiler.ast;

public class AstExpressionRewriteVisitor extends AstVisitor {
  private final AstExpressionRewriter rewriter;

  public AstExpressionRewriteVisitor(AstExpressionRewriter rewriter) {
    this.rewriter = rewriter;
  }

  @Override
  protected void visit(AstExpressionHolder node) {
    node.value(node.value().rewrite(rewriter));
    // Do not visit children. Rewriter will do this.
  }
}
