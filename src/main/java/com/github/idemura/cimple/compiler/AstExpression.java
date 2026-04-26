package com.github.idemura.cimple.compiler;

abstract class AstExpression extends AstNode {
  protected AstExpression() {}

  abstract TypeRef getTypeRef();
}
