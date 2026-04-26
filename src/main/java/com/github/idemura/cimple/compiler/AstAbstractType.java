package com.github.idemura.cimple.compiler;

abstract class AstAbstractType extends AstAbstractNode {
  private String name;

  protected AstAbstractType() {}

  String getName() {
    return name;
  }

  void setName(String name) {
    this.name = name;
  }
}
