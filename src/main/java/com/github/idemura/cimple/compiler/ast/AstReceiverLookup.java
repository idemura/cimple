package com.github.idemura.cimple.compiler.ast;

public final class AstReceiverLookup extends AstExpression {
  private AstExpression object;
  private String functionName;

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  @Override
  public AstTypeRef type() {
    return null;
  }

  public AstExpression object() {
    return object;
  }

  public void object(AstExpression object) {
    this.object = object;
  }

  public String functionName() {
    return functionName;
  }

  public void functionName(String functionName) {
    this.functionName = functionName;
  }
}
