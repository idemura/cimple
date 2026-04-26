package com.github.idemura.cimple.compiler.ast;

public abstract class AstType extends AstNode {
  private String name;

  protected AstType() {}

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
