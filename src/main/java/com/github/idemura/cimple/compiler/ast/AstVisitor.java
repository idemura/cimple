package com.github.idemura.cimple.compiler.ast;

public abstract class AstVisitor {
  protected AstVisitor() {}

  protected Object visit(AstModule node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstModule node) {
    for (var t : node.types()) {
      t.accept(this);
    }
    for (var v : node.variables()) {
      v.accept(this);
    }
    for (var f : node.functions()) {
      f.accept(this);
    }
  }

  protected Object visit(AstTypeAlias node) {
    return null;
  }

  protected Object visit(AstTypeFunction node) {
    return null;
  }

  protected Object visit(AstTypeBuiltin node) {
    return null;
  }

  protected Object visit(AstTypeUnion node) {
    return null;
  }

  protected Object visit(AstTypeStruct node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstTypeStruct node) {
    for (var field : node.getFields()) {
      field.accept(this);
    }
  }

  protected Object visit(AstVariable node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstVariable node) {
    if (node.getExpression() != null) {
      node.getExpression().accept(this);
    }
  }

  protected Object visit(AstFunction node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstFunction node) {
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

  protected Object visit(AstTypeCast node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstTypeCast node) {
    node.getExpression().accept(this);
  }

  protected Object visit(AstNameRef node) {
    return null;
  }

  protected Object visit(AstApplyFunction node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstApplyFunction node) {
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
    if (node.getExpression() != null) {
      node.getExpression().accept(this);
    }
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
    if (node.getElseBlock() != null) {
      node.getElseBlock().accept(this);
    }
  }

  protected Object visit(AstFor node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstFor node) {
    if (node.getInit() != null) {
      node.getInit().accept(this);
    }
    if (node.getCondition() != null) {
      node.getCondition().accept(this);
    }
    if (node.getIncrement() != null) {
      node.getIncrement().accept(this);
    }
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
}
