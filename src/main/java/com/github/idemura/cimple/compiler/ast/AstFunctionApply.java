package com.github.idemura.cimple.compiler.ast;

import java.util.List;

public final class AstFunctionApply extends AstExpression {
  private String functionName;
  private List<AstExpression> args;
  private AstFunction function;

  public AstFunctionApply() {}

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  @Override
  public AstType getType() {
    return function.getHeader().getResultType().getType();
  }

  public void setFunctionName(String functionName) {
    this.functionName = functionName;
  }

  public String getFunctionName() {
    return functionName;
  }

  public void setArgs(List<AstExpression> args) {
    this.args = args;
  }

  public List<AstExpression> getArgs() {
    return List.copyOf(args);
  }

  public void setFunction(AstFunction function) {
    this.function = function;
  }

  public AstFunction getFunction() {
    return function;
  }
}
