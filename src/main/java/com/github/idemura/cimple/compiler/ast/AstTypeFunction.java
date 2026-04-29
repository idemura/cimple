package com.github.idemura.cimple.compiler.ast;

public final class AstTypeFunction extends AstType {
  private AstFunctionHeader header;

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  @Override
  public QualifiedName getName() {
    return header.getName();
  }

  public void setName(QualifiedName name) {
    header.setName(name);
  }

  public void setHeader(AstFunctionHeader header) {
    this.header = header;
  }

  public AstFunctionHeader getHeader() {
    return header;
  }
}
