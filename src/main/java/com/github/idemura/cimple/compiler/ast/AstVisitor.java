package com.github.idemura.cimple.compiler.ast;

public abstract class AstVisitor {
  protected AstVisitor() {}

  protected Object visit(AstModule node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstModule node) {
    for (var definition : node.definitions()) {
      definition.accept(this);
    }
  }

  protected Object visit(AstTypeRef node) {
    return null;
  }

  protected Object visit(AstFunctionHeader node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstFunctionHeader node) {
    acceptSafe(node.getObjectType());
    for (var parameter : node.getParameters()) {
      parameter.accept(this);
    }
    acceptSafe(node.getResultType());
  }

  protected Object visit(AstFunctionType node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstFunctionType node) {
    node.getHeader().accept(this);
  }

  protected Object visit(AstBuiltinType node) {
    return null;
  }

  protected Object visit(AstUnionType node) {
    return null;
  }

  protected Object visit(AstRecordType node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstRecordType node) {
    for (var field : node.getFields()) {
      field.accept(this);
    }
  }

  protected Object visit(AstVariable node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstVariable node) {
    acceptSafe(node.getType());
    acceptSafe(node.getExpression());
  }

  protected Object visit(AstVariableStatement node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstVariableStatement node) {
    node.getVariable().accept(this);
  }

  protected Object visit(AstFunction node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstFunction node) {
    node.getHeader().accept(this);
    node.getBlock().accept(this);
  }

  protected Object visit(AstBlock node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstBlock node) {
    for (var s : node.statements()) {
      s.accept(this);
    }
  }

  protected Object visit(AstExpressionStatement node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstExpressionStatement node) {
    node.getExpression().accept(this);
  }

  protected Object visit(AstLiteral node) {
    return null;
  }

  protected Object visit(AstFieldAccess node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstFieldAccess node) {
    node.getObject().accept(this);
  }

  protected Object visit(AstBind node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstBind node) {
    node.getObject().accept(this);
  }

  protected Object visit(AstArrayAccess node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstArrayAccess node) {
    node.getArray().accept(this);
    node.getIndex().accept(this);
  }

  protected Object visit(AstCast node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstCast node) {
    node.getExpression().accept(this);
  }

  protected Object visit(AstEntityRef node) {
    return null;
  }

  protected Object visit(AstCall node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstCall node) {
    node.getFunction().accept(this);
    for (var a : node.getArgs()) {
      a.accept(this);
    }
  }

  protected Object visit(AstReturn node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstReturn node) {
    acceptSafe(node.getExpression());
  }

  protected Object visit(AstIf node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstIf node) {
    for (var condition : node.getConditions()) {
      condition.accept(this);
    }
    for (var thenBlock : node.getThenBlocks()) {
      thenBlock.accept(this);
    }
    acceptSafe(node.getElseBlock());
  }

  protected Object visit(AstFor node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstFor node) {
    acceptSafe(node.getInit());
    acceptSafe(node.getCondition());
    acceptSafe(node.getIncrement());
    node.getBlock().accept(this);
  }

  protected Object visit(AstGoto node) {
    return null;
  }

  protected Object visit(AstDefer node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstDefer node) {
    node.getBlock().accept(this);
  }

  protected void acceptSafe(AstNode node) {
    if (node != null) {
      node.accept(this);
    }
  }
}
