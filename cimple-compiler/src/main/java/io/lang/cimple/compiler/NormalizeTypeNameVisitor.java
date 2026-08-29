package io.lang.cimple.compiler;

import io.lang.cimple.compiler.ast.AstBuiltinType;
import io.lang.cimple.compiler.ast.AstType;
import io.lang.cimple.compiler.ast.AstTypeRef;
import io.lang.cimple.compiler.ast.AstVisitor;
import java.util.Objects;

class NormalizeTypeNameVisitor extends AstVisitor {
  void normalize(AstType type) {
    if (type != null) {
      type.accept(this);
    }
  }

  @Override
  protected void visit(AstTypeRef node) {
    if (Objects.equals("int", node.name().type())) {
      node.name(AstBuiltinType.INT64.name());
    }
    super.visit(node);
  }
}
