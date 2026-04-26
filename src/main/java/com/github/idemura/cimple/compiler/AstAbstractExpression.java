package com.github.idemura.cimple.compiler;

abstract class AstAbstractExpression extends AstAbstractNode {
  protected AstAbstractExpression() {}

  abstract TypeRef getTypeRef();
}
