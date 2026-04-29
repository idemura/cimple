package com.github.idemura.cimple.compiler.ast;

public abstract class AstType extends AstNode {
  private String moduleName;
  private String name;

  protected AstType() {}

  public String getModuleName() {
    return moduleName;
  }

  public void setModuleName(String moduleName) {
    this.moduleName = moduleName;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
