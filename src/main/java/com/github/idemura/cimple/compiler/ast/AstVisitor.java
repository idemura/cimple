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

  protected Object visit(AstFunctionHeader node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstFunctionHeader node) {
    // Receiver type ref visited as one of the parameters type ref.
    for (var parameter : node.parameters()) {
      parameter.accept(this);
    }
    acceptSafe(node.resultType());
  }

  protected Object visit(AstFunction node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstFunction node) {
    node.header().accept(this);
    acceptSafe(node.type());
    node.block().accept(this);
  }

  protected Object visit(AstVariable node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstVariable node) {
    acceptSafe(node.type());
    acceptSafe(node.expression());
  }

  protected Object visit(AstTypeRef node) {
    return null;
  }

  protected Object visit(AstBuiltinType node) {
    return null;
  }

  protected Object visit(AstFunctionType node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstFunctionType node) {
    node.header().accept(this);
  }

  protected Object visit(AstRecordType node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstRecordType node) {
    for (var field : node.fields()) {
      field.accept(this);
    }
  }

  protected Object visit(AstUnionType node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstUnionType node) {
    for (var variant : node.variants()) {
      acceptSafe(variant.valueType());
    }
  }

  protected Object visit(AstBlock node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstBlock node) {
    for (var statement : node.statements()) {
      statement.accept(this);
    }
  }

  protected Object visit(AstExpressionStatement node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstExpressionStatement node) {
    node.expression().accept(this);
  }

  protected Object visit(AstLocal node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstLocal node) {
    node.variable().accept(this);
  }

  protected Object visit(AstIf node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstIf node) {
    for (var condition : node.conditions()) {
      condition.accept(this);
    }
    for (var thenBlock : node.thenBlocks()) {
      thenBlock.accept(this);
    }
    acceptSafe(node.elseBlock());
  }

  protected Object visit(AstFor node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstFor node) {
    acceptSafe(node.init());
    acceptSafe(node.condition());
    acceptSafe(node.increment());
    node.block().accept(this);
  }

  protected Object visit(AstReturn node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstReturn node) {
    acceptSafe(node.expression());
  }

  protected Object visit(AstDelete node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstDelete node) {
    acceptSafe(node.expression());
  }

  protected Object visit(AstDefer node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstDefer node) {
    node.block().accept(this);
  }

  protected Object visit(AstGoto node) {
    return null;
  }

  protected Object visit(AstNullLiteral node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstNullLiteral node) {
    acceptSafe(node.type());
  }

  protected Object visit(AstBoolLiteral node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstBoolLiteral node) {
    acceptSafe(node.type());
  }

  protected Object visit(AstNumberLiteral node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstNumberLiteral node) {
    acceptSafe(node.type());
  }

  protected Object visit(AstStringLiteral node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstStringLiteral node) {
    acceptSafe(node.type());
  }

  protected Object visit(AstEntityRef node) {
    return null;
  }

  protected Object visit(AstNew node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstNew node) {
    acceptSafe(node.type());
    acceptSafe(node.size());
  }

  protected Object visit(AstCall node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstCall node) {
    node.function().accept(this);
    for (var argument : node.arguments()) {
      argument.accept(this);
    }
  }

  protected Object visit(AstArrayAccess node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstArrayAccess node) {
    node.array().accept(this);
    node.index().accept(this);
  }

  protected Object visit(AstFieldAccess node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstFieldAccess node) {
    node.object().accept(this);
  }

  protected Object visit(AstReceiverLookup node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstReceiverLookup node) {
    node.receiver().accept(this);
  }

  protected Object visit(AstCast node) {
    visitChildren(node);
    return null;
  }

  protected void visitChildren(AstCast node) {
    node.expression().accept(this);
    acceptSafe(node.type());
  }

  protected void acceptSafe(AstNode node) {
    if (node != null) {
      node.accept(this);
    }
  }
}
