package io.lang.cimple.compiler;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableList;

public final class AstCall extends AstExpression {
  private final AstFunctionRef function;
  private ImmutableMap<AstTypeWildcard, AstType> deducedWildcards = ImmutableMap.of();
  private ImmutableList<AstExpression> arguments;

  public AstCall(AstFunctionRef function, ImmutableList<AstExpression> arguments) {
    super(function.location());
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
    arguments = arguments.stream().map(a -> a.rewrite(visitor)).collect(toImmutableList());
    return visitor.rewrite(this);
  }

  @Override
  public AstType type() {
    if (!function.isResolved()) {
      return null;
    }
    return function.function().resultType().substitute(deducedWildcards);
  }

  public AstFunctionRef function() {
    return function;
  }

  public ImmutableList<AstExpression> arguments() {
    return arguments;
  }

  public ImmutableMap<AstTypeWildcard, AstType> deducedWildcards() {
    return deducedWildcards;
  }

  public void deducedWildcards(ImmutableMap<AstTypeWildcard, AstType> deducedWildcards) {
    this.deducedWildcards = deducedWildcards;
  }
}
