package com.github.idemura.cimple.compiler;

abstract class AstStatement extends AstNode {
  protected AstStatement(Location location) {
    super(location);
  }
}
