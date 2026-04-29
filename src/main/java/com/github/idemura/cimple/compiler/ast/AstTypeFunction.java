package com.github.idemura.cimple.compiler.ast;

public final class AstTypeFunction extends AstType {
  private AstFunctionHeader header;

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
}
