package com.github.idemura.cimple.compiler;

import com.github.idemura.cimple.compiler.ast.AstBuiltinType;
import com.github.idemura.cimple.compiler.ast.AstType;
import com.github.idemura.cimple.compiler.ast.AstTypeRef;
import com.github.idemura.cimple.compiler.ast.AstVisitor;
import java.util.Objects;

class NormalizeTypeNameVisitor extends AstVisitor {
  void normalize(AstType type) {
    if (type != null) {
      type.accept(this);
    }
  }

  @Override
  protected void visit(AstTypeRef node) {
    if (Objects.equals("int", node.name().typeName())) {
      node.name(AstBuiltinType.INT64.name());
    }
    super.visit(node);
  }
}
