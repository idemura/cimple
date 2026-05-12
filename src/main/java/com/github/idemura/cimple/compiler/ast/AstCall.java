package com.github.idemura.cimple.compiler.ast;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.collect.ImmutableList;
import java.util.List;

public final class AstCall extends AstExpression {
  private AstExpression function;
  private List<AstExpression> arguments;

  public AstCall() {}

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {
    function.accept(visitor);
    for (var argument : arguments) {
      argument.accept(visitor);
    }
  }

  @Override
  public AstExpression rewrite(AstExpressionRewriter rewriter) {
    function = function.rewrite(rewriter);
    arguments = arguments.stream().map(a -> a.rewrite(rewriter)).collect(toImmutableList());
    return rewriter.rewrite(this);
  }

  @Override
  public AstType type() {
    return ((AstFunctionType) function.type()).header().resultType();
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
