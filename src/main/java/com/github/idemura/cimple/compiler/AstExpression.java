package com.github.idemura.cimple.compiler;

abstract class AstExpression extends AstNode {
  protected AstExpression(Location location) {
    super(location);
  }

  abstract TypeRef getTypeRef();
}
