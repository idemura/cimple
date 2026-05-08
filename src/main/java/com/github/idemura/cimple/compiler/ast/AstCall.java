package com.github.idemura.cimple.compiler.ast;

import com.google.common.collect.ImmutableList;
import java.util.List;

public final class AstCall extends AstExpression {
  private AstExpression function;
  private List<AstExpression> arguments;

  public AstCall() {}

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  @Override
  public AstTypeRef typeRef() {
    return function.typeRef();
  }

  public AstExpression function() {
    return function;
  }

  public void function(AstExpression function) {
    this.function = function;
  }

  public List<AstExpression> arguments() {
    return arguments;
  }

  public void arguments(List<AstExpression> arguments) {
    this.arguments = ImmutableList.copyOf(arguments);
  }
}
