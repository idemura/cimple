package com.github.idemura.cimple.compiler;

public class AstVariable extends AstStatement {
  private final VariableDef variable;
  private AstExpression init;

  AstVariable(VariableDef variable, AstExpression init) {
    super(null);
    this.variable = variable;
    this.init = init;
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  VariableDef getVariableDef() {
    return variable;
  }

  void setInit(AstExpression init) {
    this.init = init;
  }

  AstExpression getInit() {
    return init;
  }
}
