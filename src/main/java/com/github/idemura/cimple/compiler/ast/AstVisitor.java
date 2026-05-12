package com.github.idemura.cimple.compiler.ast;

public abstract class AstVisitor {
  protected AstVisitor() {}

  protected void visit(AstModule node) {
    visitChildren(node);
  }

  protected void visitChildren(AstModule node) {
    for (var definition : node.definitions()) {
      definition.accept(this);
    }
  }

  protected void visit(AstFunctionHeader node) {
    visitChildren(node);
  }

  protected void visitChildren(AstFunctionHeader node) {
    // Receiver type ref visited as one of the parameters type ref.
    for (var parameter : node.parameters()) {
      parameter.accept(this);
    }
    acceptSafe(node.resultType());
  }

  protected void visit(AstFunction node) {
    visitChildren(node);
  }

  protected void visitChildren(AstFunction node) {
    node.header().accept(this);
    acceptSafe(node.type());
    node.block().accept(this);
  }

  protected void visit(AstVariable node) {
    visitChildren(node);
  }

  protected void visitChildren(AstVariable node) {
    acceptSafe(node.type());
    acceptSafe(node.expression());
  }

  protected void visit(AstTypeRef node) {}

  protected void visit(AstPointerType node) {
    visitChildren(node);
  }

  protected void visitChildren(AstPointerType node) {
    node.baseType().accept(this);
  }

  protected void visit(AstBuiltinType node) {}

  protected void visit(AstFunctionType node) {
    visitChildren(node);
  }

  protected void visitChildren(AstFunctionType node) {
    node.header().accept(this);
  }

  protected void visit(AstRecordType node) {
    visitChildren(node);
  }

  protected void visitChildren(AstRecordType node) {
    for (var field : node.fields()) {
      field.accept(this);
    }
  }

  protected void visit(AstUnionType node) {
    visitChildren(node);
  }

  protected void visitChildren(AstUnionType node) {
    for (var variant : node.variants()) {
      acceptSafe(variant.valueType());
    }
  }

  protected void visit(AstBlock node) {
    visitChildren(node);
  }

  protected void visitChildren(AstBlock node) {
    for (var statement : node.statements()) {
      statement.accept(this);
    }
  }

  protected void visit(AstExpressionStatement node) {
    visitChildren(node);
  }

  protected void visitChildren(AstExpressionStatement node) {
    node.expression().accept(this);
  }

  protected void visit(AstLocal node) {
    visitChildren(node);
  }

  protected void visitChildren(AstLocal node) {
    node.variable().accept(this);
  }

  protected void visit(AstIf node) {
    visitChildren(node);
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

  protected void visit(AstFor node) {
    visitChildren(node);
  }

  protected void visitChildren(AstFor node) {
    acceptSafe(node.init());
    acceptSafe(node.condition());
    acceptSafe(node.increment());
    node.block().accept(this);
  }

  protected void visit(AstReturn node) {
    visitChildren(node);
  }

  protected void visitChildren(AstReturn node) {
    acceptSafe(node.expression());
  }

  protected void visit(AstDelete node) {
    visitChildren(node);
  }

  protected void visitChildren(AstDelete node) {
    acceptSafe(node.expression());
  }

  protected void visit(AstDefer node) {
    visitChildren(node);
  }

  protected void visitChildren(AstDefer node) {
    node.block().accept(this);
  }

  protected void visit(AstGoto node) {}

  protected void visit(AstExpressionHolder node) {
    visitChildren(node);
  }

  private void visitChildren(AstExpressionHolder node) {
    node.root().accept(this);
  }

  protected void visit(AstNullLiteral node) {
    visitChildren(node);
  }

  protected void visitChildren(AstNullLiteral node) {
    acceptSafe(node.type());
  }

  protected void visit(AstBoolLiteral node) {
    visitChildren(node);
  }

  protected void visitChildren(AstBoolLiteral node) {
    acceptSafe(node.type());
  }

  protected void visit(AstNumberLiteral node) {
    visitChildren(node);
  }

  protected void visitChildren(AstNumberLiteral node) {
    acceptSafe(node.type());
  }

  protected void visit(AstStringLiteral node) {
    visitChildren(node);
  }

  protected void visitChildren(AstStringLiteral node) {
    acceptSafe(node.type());
  }

  protected void visit(AstEntityRef node) {}

  protected void visit(AstNew node) {
    visitChildren(node);
  }

  protected void visitChildren(AstNew node) {
    acceptSafe(node.type());
    acceptSafe(node.size());
  }

  protected void visit(AstCall node) {
    visitChildren(node);
  }

  protected void visitChildren(AstCall node) {
    node.function().accept(this);
    for (var argument : node.arguments()) {
      argument.accept(this);
    }
  }

  protected void visit(AstArrayAccess node) {
    visitChildren(node);
  }

  protected void visitChildren(AstArrayAccess node) {
    node.array().accept(this);
    node.index().accept(this);
  }

  protected void visit(AstFieldAccess node) {
    visitChildren(node);
  }

  protected void visitChildren(AstFieldAccess node) {
    node.object().accept(this);
  }

  protected void visit(AstReceiverLookup node) {
    visitChildren(node);
  }

  protected void visitChildren(AstReceiverLookup node) {
    node.receiver().accept(this);
  }

  protected void visit(AstCast node) {
    visitChildren(node);
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
