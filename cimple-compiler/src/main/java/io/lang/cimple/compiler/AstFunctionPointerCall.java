package io.lang.cimple.compiler;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.collect.ImmutableList;

public final class AstFunctionPointerCall extends AstExpression {
  private AstExpression function;
  private ImmutableList<AstExpression> arguments;

  public AstFunctionPointerCall(
      Location location, AstExpression function, ImmutableList<AstExpression> arguments) {
    super(location);
    this.function = function;
    this.arguments = arguments;
  }

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
      return functionType.function().resultType();
    }
    return AstBuiltinType.VOID;
  }

  public AstExpression function() {
    return function;
  }

  public void function(AstExpression function) {
    this.function = function;
  }

  public ImmutableList<AstExpression> arguments() {
    return arguments;
  }
}
