package com.github.idemura.cimple.compiler.ast;

public abstract class AstVisitor {
  protected AstVisitor() {}

  protected void visit(AstModule node) {
    visitChildren(node);
  }

  protected void visitChildren(AstModule node) {
    for (var t : node.getTypes()) {
      t.accept(this);
    }
    for (var v : node.getVariables()) {
      v.accept(this);
    }
    for (var f : node.getFunctions()) {
      f.accept(this);
    }
  }

  protected void visit(AstTypeStruct node) {
    visitChildren(node);
  }

  protected void visitChildren(AstTypeStruct node) {
    for (var field : node.getFields()) {
      field.accept(this);
    }
  }

  protected void visit(AstTypeAlias node) {}

  protected void visit(AstFunction node) {
    visitChildren(node);
  }

  protected void visitChildren(AstFunction node) {
    node.getBlock().accept(this);
  }

  protected void visit(AstBlock node) {
    visitChildren(node);
  }

  protected void visitChildren(AstBlock node) {
    for (var s : node.getStatements()) {
      s.accept(this);
    }
  }

  protected void visit(AstReturn node) {
    visitChildren(node);
  }

  protected void visitChildren(AstReturn node) {
    if (node.getExpression() != null) {
      node.getExpression().accept(this);
    }
  }

  protected void visit(AstLiteral node) {}

  protected void visit(AstNameRef node) {}

  protected void visit(AstFunctionApply node) {
    visitChildren(node);
  }

  protected void visitChildren(AstFunctionApply node) {
    for (var a : node.getArgs()) {
      a.accept(this);
    }
  }

  protected void visit(AstIf node) {
    visitChildren(node);
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

  protected void visit(AstFor node) {
    visitChildren(node);
  }

  protected void visitChildren(AstFor node) {
    if (node.getInit() != null) {
      node.getInit().accept(this);
    }
    if (node.getCondition() != null) {
      node.getCondition().accept(this);
    }
    node.getBlock().accept(this);
  }

  protected void visit(AstGoto node) {}

  protected void visit(AstDefer node) {
    visitChildren(node);
  }

  protected void visitChildren(AstDefer node) {
    node.getExpression().accept(this);
  }

  protected void visit(AstVariable node) {
    visitChildren(node);
  }

  protected void visitChildren(AstVariable node) {
    if (node.getInit() != null) {
      node.getInit().accept(this);
    }
  }

  protected void visit(AstExpressionStatement node) {
    visitChildren(node);
  }

  protected void visitChildren(AstExpressionStatement node) {
    node.getExpression().accept(this);
  }
}
