package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.QualifiedName;

public final class AstFunctionType extends AstType {
  private AstFunctionHeader header;

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  @Override
  public QualifiedName name() {
    return header.name();
  }

  public void name(QualifiedName name) {
    header.name(name);
  }

  public AstFunctionHeader header() {
    return header;
  }

  public void header(AstFunctionHeader header) {
    this.header = header;
  }
}
