package com.github.idemura.cimple.compiler.semantics;

import com.github.idemura.cimple.compiler.ErrorConsumer;
import com.github.idemura.cimple.compiler.ast.AstArrayAccess;
import com.github.idemura.cimple.compiler.ast.AstBind;
import com.github.idemura.cimple.compiler.ast.AstBlock;
import com.github.idemura.cimple.compiler.ast.AstBoolLiteral;
import com.github.idemura.cimple.compiler.ast.AstCall;
import com.github.idemura.cimple.compiler.ast.AstCast;
import com.github.idemura.cimple.compiler.ast.AstDefer;
import com.github.idemura.cimple.compiler.ast.AstEntityRef;
import com.github.idemura.cimple.compiler.ast.AstExpressionStatement;
import com.github.idemura.cimple.compiler.ast.AstFieldAccess;
import com.github.idemura.cimple.compiler.ast.AstFor;
import com.github.idemura.cimple.compiler.ast.AstFunction;
import com.github.idemura.cimple.compiler.ast.AstFunctionHeader;
import com.github.idemura.cimple.compiler.ast.AstFunctionType;
import com.github.idemura.cimple.compiler.ast.AstGoto;
import com.github.idemura.cimple.compiler.ast.AstIf;
import com.github.idemura.cimple.compiler.ast.AstLet;
import com.github.idemura.cimple.compiler.ast.AstModule;
import com.github.idemura.cimple.compiler.ast.AstNullLiteral;
import com.github.idemura.cimple.compiler.ast.AstNumberLiteral;
import com.github.idemura.cimple.compiler.ast.AstRecordType;
import com.github.idemura.cimple.compiler.ast.AstReturn;
import com.github.idemura.cimple.compiler.ast.AstStringLiteral;
import com.github.idemura.cimple.compiler.ast.AstTypeRef;
import com.github.idemura.cimple.compiler.ast.AstUnionType;
import com.github.idemura.cimple.compiler.ast.AstVariable;
import com.github.idemura.cimple.compiler.ast.AstVisitor;

class TypeCheckVisitor extends AstVisitor {
  private final ErrorConsumer errorConsumer;

  public TypeCheckVisitor(ErrorConsumer errorConsumer) {
    this.errorConsumer = errorConsumer;
  }

  @Override
  protected Object visit(AstModule node) {
    return super.visit(node);
  }

  @Override
  protected Object visit(AstFunctionHeader node) {
    return super.visit(node);
  }

  @Override
  protected Object visit(AstFunction node) {
    return super.visit(node);
  }

  @Override
  protected Object visit(AstVariable node) {
    return super.visit(node);
  }

  @Override
  protected Object visit(AstTypeRef node) {
    return super.visit(node);
  }

  @Override
  protected Object visit(AstFunctionType node) {
    return super.visit(node);
  }

  @Override
  protected Object visit(AstRecordType node) {
    return super.visit(node);
  }

  @Override
  protected Object visit(AstUnionType node) {
    return super.visit(node);
  }

  @Override
  protected Object visit(AstBlock node) {
    return super.visit(node);
  }

  @Override
  protected Object visit(AstExpressionStatement node) {
    return super.visit(node);
  }

  @Override
  protected Object visit(AstLet node) {
    return super.visit(node);
  }

  @Override
  protected Object visit(AstIf node) {
    return super.visit(node);
  }

  @Override
  protected Object visit(AstFor node) {
    return super.visit(node);
  }

  @Override
  protected Object visit(AstReturn node) {
    return super.visit(node);
  }

  @Override
  protected Object visit(AstDefer node) {
    return super.visit(node);
  }

  @Override
  protected Object visit(AstGoto node) {
    return super.visit(node);
  }

  @Override
  protected Object visit(AstNullLiteral node) {
    return super.visit(node);
  }

  @Override
  protected Object visit(AstBoolLiteral node) {
    return super.visit(node);
  }

  @Override
  protected Object visit(AstNumberLiteral node) {
    return super.visit(node);
  }

  @Override
  protected Object visit(AstStringLiteral node) {
    return super.visit(node);
  }

  @Override
  protected Object visit(AstEntityRef node) {
    return super.visit(node);
  }

  @Override
  protected Object visit(AstCall node) {
    return super.visit(node);
  }

  @Override
  protected Object visit(AstArrayAccess node) {
    return super.visit(node);
  }

  @Override
  protected Object visit(AstFieldAccess node) {
    return super.visit(node);
  }

  @Override
  protected Object visit(AstBind node) {
    return super.visit(node);
  }

  @Override
  protected Object visit(AstCast node) {
    return super.visit(node);
  }
}
