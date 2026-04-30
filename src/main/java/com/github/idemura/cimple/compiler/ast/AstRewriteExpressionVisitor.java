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
  protected Object visit(AstFieldAccess node) {
    visitChildren(node);
    return node;
  }

  @Override
  protected void visitChildren(AstFieldAccess node) {
    node.setObject(rewrite(node.getObject()));
  }

  @Override
  protected Object visit(AstBind node) {
    visitChildren(node);
    return node;
  }

  @Override
  protected void visitChildren(AstBind node) {
    node.setObject(rewrite(node.getObject()));
  }

  @Override
  protected Object visit(AstArrayAccess node) {
    visitChildren(node);
    return node;
  }

  @Override
  protected void visitChildren(AstArrayAccess node) {
    node.setArray(rewrite(node.getArray()));
    node.setIndex(rewrite(node.getIndex()));
  }

  @Override
  protected Object visit(AstCast node) {
    visitChildren(node);
    return node;
  }

  @Override
  protected void visitChildren(AstCast node) {
    node.setExpression(rewrite(node.getExpression()));
  }

  @Override
  protected Object visit(AstName node) {
    return node;
  }

  @Override
  protected Object visit(AstCall node) {
    visitChildren(node);
    return node;
  }

  @Override
  protected void visitChildren(AstCall node) {
    node.setFunction(rewrite(node.getFunction()));
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
    node.setIncrement(rewrite(node.getIncrement()));
    node.getBlock().accept(this);
  }

  @Override
  protected void visitChildren(AstDefer node) {
    node.getBlock().accept(this);
  }

  private AstExpression rewrite(AstExpression expr) {
    if (expr != null) {
      return (AstExpression) expr.accept(this);
    }
    return null;
  }
}
