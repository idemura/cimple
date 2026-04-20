package com.github.idemura.cimple.compiler;

import java.util.ArrayList;
import java.util.List;

public abstract class Visitor {
  private final List<VisitorNode> stack = new ArrayList<>();

  protected VisitorNode getParent() {
    return getParent(0);
  }

  protected VisitorNode getParent(int n) {
    return n < stack.size() ? stack.get(stack.size() - (n + 1)) : null;
  }

  protected void visit(AstModule node) {
    visitChildren(node);
  }

  protected void visitChildren(AstModule node) {
    stack.add(node);
    for (var v : node.getVariables()) {
      v.accept(this);
    }
    for (var f : node.getFunctions()) {
      f.accept(this);
    }
    stack.removeLast();
  }

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

  protected void visit(AstIfElse node) {
    visitChildren(node);
  }

  protected void visitChildren(AstIfElse node) {
    stack.add(node);
    node.getCondition().accept(this);
    node.getThenBlock().accept(this);
    node.getElseBlock().accept(this);
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

  protected void visit(AstExpressionStmt node) {
    visitChildren(node);
  }

  protected void visitChildren(AstExpressionStmt node) {
    stack.add(node);
    node.getExpression().accept(this);
    stack.removeLast();
  }
}
