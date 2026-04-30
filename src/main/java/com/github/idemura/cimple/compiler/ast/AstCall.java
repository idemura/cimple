package com.github.idemura.cimple.compiler.ast;

import com.google.common.collect.ImmutableList;
import java.util.List;

public final class AstCall extends AstExpression {
  private AstExpression function;
  private List<AstExpression> args;

  public AstCall() {}

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  @Override
  public TypeRef getType() {
    return function.getType();
  }

  public AstExpression getFunction() {
    return function;
  }

  public void setFunction(AstExpression function) {
    this.function = function;
  }

  public List<AstExpression> getArgs() {
    return args;
  }

  public void setArgs(List<AstExpression> args) {
    this.args = ImmutableList.copyOf(args);
  }
}
