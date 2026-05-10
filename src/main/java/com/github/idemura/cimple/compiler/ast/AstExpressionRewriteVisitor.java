package com.github.idemura.cimple.compiler.ast;

public class AstExpressionRewriteVisitor extends AstVisitor {
  protected AstExpressionRewriteVisitor() {}

  @Override
  protected void visitChildren(AstVariable node) {
    acceptSafe(node.typeRef());
    node.expression(rewrite(node.expression()));
  }

  @Override
  protected void visitChildren(AstBlock node) {
    super.visitChildren(node);
  }

  @Override
  protected void visitChildren(AstExpressionStatement node) {
    node.expression(rewrite(node.expression()));
  }

  @Override
  protected void visitChildren(AstLocal node) {
    super.visitChildren(node);
  }

  @Override
  protected void visitChildren(AstIf node) {
    node.conditions(node.conditions().stream().map(this::rewrite).toList());
    for (var thenBlock : node.thenBlocks()) {
      thenBlock.accept(this);
    }
    if (node.elseBlock() != null) {
      node.elseBlock().accept(this);
    }
  }

  @Override
  protected void visitChildren(AstFor node) {
    if (node.init() != null) {
      node.init().accept(this);
    }
    node.condition(rewrite(node.condition()));
    node.increment(rewrite(node.increment()));
    node.block().accept(this);
  }

  @Override
  protected void visitChildren(AstReturn node) {
    node.expression(rewrite(node.expression()));
  }

  @Override
  protected void visitChildren(AstDefer node) {
    node.block().accept(this);
  }

  @Override
  protected Object visit(AstNullLiteral node) {
    return node;
  }

  @Override
  protected Object visit(AstBoolLiteral node) {
    return node;
  }

  @Override
  protected Object visit(AstNumberLiteral node) {
    return node;
  }

  @Override
  protected Object visit(AstStringLiteral node) {
    return node;
  }

  @Override
  protected Object visit(AstEntityRef node) {
    return node;
  }

  @Override
  protected Object visit(AstNew node) {
    visitChildren(node);
    return node;
  }

  @Override
  protected void visitChildren(AstNew node) {
    acceptSafe(node.typeRef());
    node.size(rewrite(node.size()));
  }

  @Override
  protected Object visit(AstCall node) {
    visitChildren(node);
    return node;
  }

  @Override
  protected void visitChildren(AstCall node) {
    node.function(rewrite(node.function()));
    node.arguments(node.arguments().stream().map(this::rewrite).toList());
  }

  @Override
  protected Object visit(AstArrayAccess node) {
    visitChildren(node);
    return node;
  }

  @Override
  protected void visitChildren(AstArrayAccess node) {
    node.array(rewrite(node.array()));
    node.index(rewrite(node.index()));
  }

  @Override
  protected Object visit(AstFieldAccess node) {
    visitChildren(node);
    return node;
  }

  @Override
  protected void visitChildren(AstFieldAccess node) {
    node.object(rewrite(node.object()));
  }

  @Override
  protected Object visit(AstReceiverLookup node) {
    visitChildren(node);
    return node;
  }

  @Override
  protected void visitChildren(AstReceiverLookup node) {
    node.receiver(rewrite(node.receiver()));
  }

  @Override
  protected Object visit(AstCast node) {
    visitChildren(node);
    return node;
  }

  @Override
  protected void visitChildren(AstCast node) {
    node.expression(rewrite(node.expression()));
  }

  private AstExpression rewrite(AstExpression expression) {
    if (expression != null) {
      return (AstExpression) expression.accept(this);
    }
    return null;
  }
}
