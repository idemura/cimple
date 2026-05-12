package com.github.idemura.cimple.compiler.ast;

public abstract class AstVisitor {
  protected AstVisitor() {}

  protected void visit(AstModule node) {
    node.acceptChildren(this);
  }

  protected void visit(AstFunctionHeader node) {
    node.acceptChildren(this);
  }

  protected void visit(AstFunction node) {
    node.acceptChildren(this);
  }

  protected void visit(AstVariable node) {
    node.acceptChildren(this);
  }

  protected void visit(AstTypeHolder node) {
    node.acceptChildren(this);
  }

  protected void visit(AstTypeRef node) {
    node.acceptChildren(this);
  }

  protected void visit(AstPointerType node) {
    node.acceptChildren(this);
  }

  protected void visit(AstBuiltinType node) {
    node.acceptChildren(this);
  }

  protected void visit(AstStringType node) {
    node.acceptChildren(this);
  }

  protected void visit(AstFunctionType node) {
    node.acceptChildren(this);
  }

  protected void visit(AstRecordType node) {
    node.acceptChildren(this);
  }

  protected void visit(AstUnionType node) {
    node.acceptChildren(this);
  }

  protected void visit(AstBlock node) {
    node.acceptChildren(this);
  }

  protected void visit(AstExpressionStatement node) {
    node.acceptChildren(this);
  }

  protected void visit(AstLocal node) {
    node.acceptChildren(this);
  }

  protected void visit(AstIf node) {
    node.acceptChildren(this);
  }

  protected void visit(AstFor node) {
    node.acceptChildren(this);
  }

  protected void visit(AstReturn node) {
    node.acceptChildren(this);
  }

  protected void visit(AstDelete node) {
    node.acceptChildren(this);
  }

  protected void visit(AstDefer node) {
    node.acceptChildren(this);
  }

  protected void visit(AstGoto node) {
    node.acceptChildren(this);
  }

  protected void visit(AstExpressionHolder node) {
    node.acceptChildren(this);
  }

  protected void visit(AstNullLiteral node) {
    node.acceptChildren(this);
  }

  protected void visit(AstBoolLiteral node) {
    node.acceptChildren(this);
  }

  protected void visit(AstNumberLiteral node) {
    node.acceptChildren(this);
  }

  protected void visit(AstStringLiteral node) {
    node.acceptChildren(this);
  }

  protected void visit(AstEntityRef node) {
    node.acceptChildren(this);
  }

  protected void visit(AstNew node) {
    node.acceptChildren(this);
  }

  protected void visit(AstCall node) {
    node.acceptChildren(this);
  }

  protected void visit(AstArrayAccess node) {
    node.acceptChildren(this);
  }

  protected void visit(AstFieldAccess node) {
    node.acceptChildren(this);
  }

  protected void visit(AstReceiverLookup node) {
    node.acceptChildren(this);
  }

  protected void visit(AstCast node) {
    node.acceptChildren(this);
  }
}
