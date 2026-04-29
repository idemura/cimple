package com.github.idemura.cimple.compiler.ast;

import java.util.List;

public final class AstTypeFunction extends AstType {
  private AstFunctionHeader header = new AstFunctionHeader();

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  public void setHeader(AstFunctionHeader header) {
    this.header = header;
  }

  public AstFunctionHeader getHeader() {
    return header;
  }

  @Override
  public void setName(String name) {
    header.setName(name);
  }

  @Override
  public String getName() {
    return header.getName();
  }

  public void setBoundTypeName(String boundTypeName) {
    header.setBoundTypeName(boundTypeName);
  }

  public String getBoundTypeName() {
    return header.getBoundTypeName();
  }

  public void setResultType(com.github.idemura.cimple.compiler.TypeRef resultType) {
    header.setResultType(resultType);
  }

  public com.github.idemura.cimple.compiler.TypeRef getResultType() {
    return header.getResultType();
  }

  public void addParameter(com.github.idemura.cimple.compiler.VariableDef parameter) {
    header.addParameter(parameter);
  }

  public List<com.github.idemura.cimple.compiler.VariableDef> getParameters() {
    return header.getParameters();
  }
}
