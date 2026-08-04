package com.github.idemura.cimple.compiler.semantics;

import com.github.idemura.cimple.compiler.ast.AstBuiltinType;
import com.github.idemura.cimple.compiler.ast.AstType;
import com.github.idemura.cimple.compiler.ast.AstTypeRef;
import com.github.idemura.cimple.compiler.ast.AstVisitor;

class NormalizeTypeNameVisitor extends AstVisitor {
  void normalize(AstType type) {
    if (type != null) {
      type.accept(this);
    }
  }

  @Override
  protected void visit(AstTypeRef node) {
    switch (node.name().typeName()) {
      case "int":
        node.name(AstBuiltinType.INT64.name());
        break;
      case "float":
        node.name(AstBuiltinType.FLOAT64.name());
        break;
      default:
        break;
    }
    super.visit(node);
  }
}
