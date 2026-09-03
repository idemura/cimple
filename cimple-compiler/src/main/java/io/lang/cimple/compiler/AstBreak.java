package io.lang.cimple.compiler;

public final class AstBreak extends AstStatement {
  public AstBreak(Location location) {
    super(location);
  }

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {}

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public boolean equals(Object object) {
    return object instanceof AstBreak;
  }
}
