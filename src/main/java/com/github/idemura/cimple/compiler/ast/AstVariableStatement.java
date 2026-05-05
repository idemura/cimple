package com.github.idemura.cimple.compiler.ast;

public final class AstVariableStatement extends AstStatement {
  private AstVariable variable;

  public AstVariableStatement() {}

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
