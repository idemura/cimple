package com.github.idemura.cimple.compiler.ast;

public final class AstLocal extends AstStatement {
  private AstVariable variable;

  public AstLocal() {}

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  public AstVariable getVariable() {
    return variable;
  }

  public void setVariable(AstVariable variable) {
    this.variable = variable;
  }
}
