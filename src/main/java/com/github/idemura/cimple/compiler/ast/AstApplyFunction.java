package com.github.idemura.cimple.compiler.ast;

import com.google.common.collect.ImmutableList;
import java.util.List;

public final class AstApplyFunction extends AstExpression {
  private QualifiedName name;
  private List<AstExpression> args;
  private AstFunction function;

  public AstApplyFunction() {}

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  @Override
  public AstType getType() {
    return function.getHeader().getResultType().getType();
  }

  public QualifiedName getName() {
    return name;
  }

  public void setName(QualifiedName name) {
    this.name = name;
  }

  public List<AstExpression> getArgs() {
    return ImmutableList.copyOf(args);
  }

  public void setArgs(List<AstExpression> args) {
    this.args = ImmutableList.copyOf(args);
  }

  public AstFunction getFunction() {
    return function;
  }

  public void setFunction(AstFunction function) {
    this.function = function;
  }
}
