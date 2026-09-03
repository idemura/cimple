package io.lang.cimple.compiler;

public final class AstLocal extends AstStatement {
  private final AstVariable variable;

  public AstLocal(AstVariable variable) {
    super(null);
    this.variable = variable;
  }

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {
    variable.accept(visitor);
  }

  public AstVariable variable() {
    return variable;
  }
}
