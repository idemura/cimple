package com.github.idemura.cimple.compiler.ast;

public class AstRewriteExpressionVisitor extends AstVisitor {
  protected AstRewriteExpressionVisitor() {}

  @Override
  protected void visitChildren(AstVariable node) {
    node.setExpression(rewrite(node.getExpression()));
  }

  @Override
  protected void visitChildren(AstExpressionStatement node) {
    node.setExpression(rewrite(node.getExpression()));
  }

  @Override
  protected Object visit(AstLiteral node) {
    return node;
  }

  @Override
  protected Object visit(AstNameRef node) {
    return node;
  }

  @Override
  protected Object visit(AstFunctionApply node) {
    visitChildren(node);
    return node;
  }

  @Override
  protected void visitChildren(AstFunctionApply node) {
    node.setArgs(node.getArgs().stream().map(this::rewrite).toList());
  }

  @Override
  protected void visitChildren(AstReturn node) {
    node.setExpression(rewrite(node.getExpression()));
  }

  @Override
  protected void visitChildren(AstIf node) {
    node.setConditions(node.getConditions().stream().map(this::rewrite).toList());
    for (var thenBlock : node.getThenBlocks()) {
      thenBlock.accept(this);
    }
    if (node.getElseBlock() != null) {
      node.getElseBlock().accept(this);
    }
  }

  @Override
  protected void visitChildren(AstFor node) {
    if (node.getInit() != null) {
      node.getInit().accept(this);
    }
    node.setCondition(rewrite(node.getCondition()));
    node.getBlock().accept(this);
  }

  @Override
  protected void visitChildren(AstDefer node) {
    node.setExpression(rewrite(node.getExpression()));
  }

  private AstExpression rewrite(AstExpression expr) {
    if (expr != null) {
      return (AstExpression) expr.accept(this);
    }
    return null;
  }
}
