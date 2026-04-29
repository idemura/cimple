package com.github.idemura.cimple.compiler.ast;

import java.util.List;

public final class AstFunction extends AstNode {
  private AstFunctionHeader header = new AstFunctionHeader();
  private AstBlock block;

  @Override
  public int hashCode() {
    return getName().hashCode();
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    return (object instanceof AstFunction other) && getName().equals(other.getName());
  }

  @Override
  public String toString() {
    return "FUNCTION %s(%s): %s".formatted(getName(), getParameters(), getResultType());
  }

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

  public void setName(String name) {
    header.setName(name);
  }

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

  public void addParameter(AstVariable parameter) {
    header.addParameter(parameter);
  }

  public List<AstVariable> getParameters() {
    return header.getParameters();
  }

  public void setBlock(AstBlock block) {
    this.block = block;
  }

  public AstBlock getBlock() {
    return block;
  }
}
