package com.github.idemura.cimple.compiler.codegen.c;

import com.github.idemura.cimple.compiler.ast.AstFunction;
import com.github.idemura.cimple.compiler.ast.AstModule;
import com.github.idemura.cimple.compiler.ast.AstVariable;
import com.github.idemura.cimple.compiler.ast.AstVisitor;

class CCodeGeneratorVisitor extends AstVisitor {
  CCodeGeneratorVisitor() {}

  @Override
  protected Object visit(AstModule node) {
    // TODO: Emit C definitions for the module body.
    return null;
  }

  @Override
  protected Object visit(AstFunction node) {
    // TODO: Emit a C function definition.
    return null;
  }

  @Override
  protected Object visit(AstVariable node) {
    // TODO: Emit a C global variable definition.
    return null;
  }
}
