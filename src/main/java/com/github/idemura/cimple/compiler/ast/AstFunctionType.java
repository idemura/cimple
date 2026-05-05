package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.QualifiedName;

public final class AstFunctionType extends AstType {
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

  public AstFunctionHeader getHeader() {
    return header;
  }

  public void setHeader(AstFunctionHeader header) {
    this.header = header;
  }
}
