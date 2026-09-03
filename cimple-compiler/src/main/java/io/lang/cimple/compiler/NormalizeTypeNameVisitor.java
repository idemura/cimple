package io.lang.cimple.compiler;

import java.util.Objects;

class NormalizeTypeNameVisitor extends AstVisitor {
  void normalize(AstType type) {
    if (type != null) {
      type.accept(this);
    }
  }

  @Override
  protected void visit(AstTypeRef node) {
    if (Objects.equals("int", node.name().entity())) {
      node.name().assign(AstBuiltinType.INT64.name());
    }
    super.visit(node);
  }
}
