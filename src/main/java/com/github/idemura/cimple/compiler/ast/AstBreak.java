package com.github.idemura.cimple.compiler.ast;

public final class AstBreak extends AstStatement {
  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {}

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public boolean equals(Object object) {
    return object instanceof AstBreak;
  }
}
