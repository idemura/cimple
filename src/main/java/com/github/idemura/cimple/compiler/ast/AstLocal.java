package com.github.idemura.cimple.compiler.ast;

public final class AstLocal extends AstStatement {
  private AstVariable variable;

  public AstLocal() {}

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  public AstVariable variable() {
    return variable;
  }

  public void variable(AstVariable variable) {
    this.variable = variable;
  }
}
