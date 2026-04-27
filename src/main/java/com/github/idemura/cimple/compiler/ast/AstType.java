package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.Type;

public abstract class AstType extends AstNode implements Type {
  private String moduleName;
  private String name;

  protected AstType() {}

  @Override
  public String getModuleName() {
    return moduleName;
  }

  public void setModuleName(String moduleName) {
    this.moduleName = moduleName;
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
