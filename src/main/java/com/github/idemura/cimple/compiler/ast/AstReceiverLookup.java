package com.github.idemura.cimple.compiler.ast;

public final class AstReceiverLookup extends AstExpression {
  private AstExpression receiver;
  private String functionName;

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  @Override
  public AstTypeRef type() {
    return null;
  }

  public AstExpression receiver() {
    return receiver;
  }

  public void receiver(AstExpression receiver) {
    this.receiver = receiver;
  }

  public String functionName() {
    return functionName;
  }

  public void functionName(String functionName) {
    this.functionName = functionName;
  }
}
