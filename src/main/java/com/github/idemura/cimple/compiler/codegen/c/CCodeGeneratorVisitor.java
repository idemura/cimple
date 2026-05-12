package com.github.idemura.cimple.compiler.codegen.c;

import com.github.idemura.cimple.compiler.ast.AstFunction;
import com.github.idemura.cimple.compiler.ast.AstModule;
import com.github.idemura.cimple.compiler.ast.AstVariable;
import com.github.idemura.cimple.compiler.ast.AstVisitor;

class CCodeGeneratorVisitor extends AstVisitor {
  CCodeGeneratorVisitor() {}

  @Override
  protected void visit(AstModule node) {
    // TODO: Emit C definitions for the module body.
  }

  @Override
  protected void visit(AstFunction node) {
    // TODO: Emit a C function definition.
  }

  @Override
  protected void visit(AstVariable node) {
    // TODO: Emit a C global variable definition.
  }
}
