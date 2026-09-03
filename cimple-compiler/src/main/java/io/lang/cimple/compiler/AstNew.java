package io.lang.cimple.compiler;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.collect.ImmutableList;

public final class AstNew extends AstExpression {
  private final AstTypeHolder type;
  private ImmutableList<AstExpression> arguments;

  public AstNew(Location location, AstType type, ImmutableList<AstExpression> arguments) {
    super(location);
    this.type = new AstTypeHolder(type);
    this.arguments = arguments;
  }

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
    var newType = type instanceof AstArrayType ? type : new AstPointerType(type);
    this.type.set(newType);
  }

  public ImmutableList<AstExpression> arguments() {
    return arguments;
  }

  public void arguments(ImmutableList<AstExpression> arguments) {
    this.arguments = arguments;
  }
}
