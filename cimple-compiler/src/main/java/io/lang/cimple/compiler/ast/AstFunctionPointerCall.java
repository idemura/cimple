package io.lang.cimple.compiler.ast;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.collect.ImmutableList;
import java.util.List;

public final class AstFunctionPointerCall extends AstExpression {
  private AstExpression function;
  private List<AstExpression> arguments;

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
  public AstExpression rewrite(AstExpressionRewriteVisitor visitor) {
    function = function.rewrite(visitor);
    arguments = arguments.stream().map(a -> a.rewrite(visitor)).collect(toImmutableList());
    return visitor.rewrite(this);
  }

  @Override
  public AstType type() {
    if (function.type() instanceof AstFunctionType functionType) {
      return functionType.header().resultType();
    }
    return null;
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
