package com.github.idemura.cimple.compiler.semantics;

import com.github.idemura.cimple.compiler.ast.AstLiteral;
import com.github.idemura.cimple.compiler.ast.AstNameRef;
import com.github.idemura.cimple.compiler.ast.AstRewriteExpressionVisitor;

class PreprocessRewriteVisitor extends AstRewriteExpressionVisitor {
  PreprocessRewriteVisitor() {}

  @Override
  protected Object visit(AstNameRef node) {
    return switch (node.getName().name()) {
      case "true" -> AstLiteral.TRUE;
      case "false" -> AstLiteral.FALSE;
      case "null" -> AstLiteral.NULL;
      default -> node;
    };
  }
}
