package com.github.idemura.cimple.compiler.semantics;

import com.github.idemura.cimple.compiler.ErrorConsumer;
import com.github.idemura.cimple.compiler.ast.AstBlock;
import com.github.idemura.cimple.compiler.ast.AstCall;
import com.github.idemura.cimple.compiler.ast.AstEntityRef;
import com.github.idemura.cimple.compiler.ast.AstFor;
import com.github.idemura.cimple.compiler.ast.AstFunction;
import com.github.idemura.cimple.compiler.ast.AstIf;
import com.github.idemura.cimple.compiler.ast.AstLiteral;
import com.github.idemura.cimple.compiler.ast.AstModule;
import com.github.idemura.cimple.compiler.ast.AstVariable;
import com.github.idemura.cimple.compiler.ast.AstVariableStatement;
import com.github.idemura.cimple.compiler.ast.AstVisitor;

class TypeCheckVisitor extends AstVisitor {
  private final ErrorConsumer errorConsumer;
  private final AstEntityNameMap variables = new AstEntityNameMap();

  public TypeCheckVisitor(ErrorConsumer errorConsumer) {
    this.errorConsumer = errorConsumer;
  }

  @Override
  protected Object visit(AstModule node) {
    return super.visit(node);
  }

  @Override
  protected Object visit(AstVariable node) {
    return super.visit(node);
  }

  @Override
  protected Object visit(AstVariableStatement node) {
    return super.visit(node);
  }

  @Override
  protected Object visit(AstFunction node) {
    variables.pushScope();
    visitChildren(node);
    variables.popScope();
    return null;
  }

  @Override
  protected Object visit(AstBlock node) {
    variables.pushScope();
    visitChildren(node);
    variables.popScope();
    return null;
  }

  @Override
  protected Object visit(AstLiteral node) {
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
  protected Object visit(AstIf node) {
    return super.visit(node);
  }

  @Override
  protected Object visit(AstFor node) {
    return super.visit(node);
  }
}
