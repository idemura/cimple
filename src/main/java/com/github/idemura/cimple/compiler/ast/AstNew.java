package com.github.idemura.cimple.compiler.ast;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.collect.ImmutableList;
import java.util.List;

public final class AstNew extends AstExpression {
  private final AstTypeHolder type = new AstTypeHolder();
  private List<AstExpression> arguments;

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {
    acceptSafe(type, visitor);
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
    return type.get();
  }

  public void type(AstType type) {
    this.type.set(type);
  }

  public List<AstExpression> arguments() {
    return arguments;
  }

  public void arguments(List<AstExpression> arguments) {
    this.arguments = ImmutableList.copyOf(arguments);
  }
}
