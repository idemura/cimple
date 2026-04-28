package com.github.idemura.cimple.compiler.semantics;

import com.github.idemura.cimple.compiler.ast.AstModule;
import com.github.idemura.cimple.compiler.ast.AstVisitor;

/// * Checks for reserved words
/// * Resolves true/false/null literals
class PrepareAstVisitor extends AstVisitor {
  PrepareAstVisitor() {}

  @Override
  protected Object visit(AstModule module) {
    return null;
  }
}
