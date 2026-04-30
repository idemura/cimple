package com.github.idemura.cimple.compiler.ast;

public final class AstBind extends AstExpression {
  private AstExpression object;
  private String functionName;

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  @Override
  public AstType getType() {
    return null;
  }

  public AstExpression getObject() {
    return object;
  }

  public void setObject(AstExpression object) {
    this.object = object;
  }

  public String getFunctionName() {
    return functionName;
  }

  public void setFunctionName(String functionName) {
    this.functionName = functionName;
  }
}
