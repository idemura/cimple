package com.github.idemura.cimple.compiler;

import java.util.ArrayList;
import java.util.List;

public abstract class AstVisitor {
  private final List<AstAbstractNode> stack = new ArrayList<>();

  protected AstAbstractNode getParent() {
    return getParent(0);
  }

  protected AstAbstractNode getParent(int n) {
    return n < stack.size() ? stack.get(stack.size() - (n + 1)) : null;
  }

  protected void visit(AstModule node) {
    visitChildren(node);
  }

  protected void visitChildren(AstModule node) {
    stack.add(node);
    for (var t : node.getTypes()) {
      t.accept(this);
    }
    for (var v : node.getVariables()) {
      v.accept(this);
    }
    for (var f : node.getFunctions()) {
      f.accept(this);
    }
    stack.removeLast();
  }

  protected void visit(AstTypeStruct node) {
    visitChildren(node);
  }

  protected void visitChildren(AstTypeStruct node) {
    stack.add(node);
    for (var field : node.getFields()) {
      field.accept(this);
    }
    stack.removeLast();
  }

  protected void visit(AstTypeAlias node) {
    visitChildren(node);
  }

  protected void visitChildren(AstTypeAlias node) {}

  protected void visit(AstFunction node) {
    visitChildren(node);
  }

  protected void visitChildren(AstFunction node) {
    stack.add(node);
    node.getBlock().accept(this);
    stack.removeLast();
  }

  protected void visit(AstBlock node) {
    visitChildren(node);
  }

  protected void visitChildren(AstBlock node) {
    stack.add(node);
    for (var s : node.getStatements()) {
      s.accept(this);
    }
    stack.removeLast();
  }

  protected void visit(AstReturn node) {
    visitChildren(node);
  }

  protected void visitChildren(AstReturn node) {
    if (node.getExpression() != null) {
      stack.add(node);
      node.getExpression().accept(this);
      stack.removeLast();
    }
  }

  protected void visit(AstLiteral node) {
    visitChildren(node);
  }

  protected void visitChildren(AstLiteral node) {}

  protected void visit(AstFunctionApply node) {
    visitChildren(node);
  }

  protected void visitChildren(AstFunctionApply node) {
    stack.add(node);
    for (var a : node.getArgs()) {
      a.accept(this);
    }
    stack.removeLast();
  }

  protected void visit(AstNameRef node) {
    visitChildren(node);
  }

  protected void visitChildren(AstNameRef node) {}

  protected void visit(AstIf node) {
    visitChildren(node);
  }

  protected void visitChildren(AstIf node) {
    stack.add(node);
    for (var condition : node.getConditions()) {
      condition.accept(this);
    }
    for (var thenBlock : node.getThenBlocks()) {
      thenBlock.accept(this);
    }
    if (node.getElseBlock() != null) {
      node.getElseBlock().accept(this);
    }
    stack.removeLast();
  }

  protected void visit(AstFor node) {
    visitChildren(node);
  }

  protected void visitChildren(AstFor node) {
    stack.add(node);
    if (node.getInit() != null) {
      node.getInit().accept(this);
    }
    if (node.getCondition() != null) {
      node.getCondition().accept(this);
    }
    node.getBlock().accept(this);
    stack.removeLast();
  }

  protected void visit(AstGoto node) {
    visitChildren(node);
  }

  protected void visitChildren(AstGoto node) {}

  protected void visit(AstDefer node) {
    visitChildren(node);
  }

  protected void visitChildren(AstDefer node) {
    stack.add(node);
    node.getExpression().accept(this);
    stack.removeLast();
  }

  protected void visit(AstVariable node) {
    visitChildren(node);
  }

  protected void visitChildren(AstVariable node) {
    if (node.getInit() != null) {
      stack.add(node);
      node.getInit().accept(this);
      stack.removeLast();
    }
  }

  protected void visit(AstExpressionStatement node) {
    visitChildren(node);
  }

  protected void visitChildren(AstExpressionStatement node) {
    stack.add(node);
    node.getExpression().accept(this);
    stack.removeLast();
  }
}
