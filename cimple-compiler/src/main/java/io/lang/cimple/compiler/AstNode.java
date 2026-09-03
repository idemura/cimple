package io.lang.cimple.compiler;

public abstract class AstNode {
  private final Location location;

  public AstNode() {
    this(null);
  }

  public AstNode(Location location) {
    this.location = location;
  }

  public abstract void accept(AstVisitor visitor);

  public abstract void acceptChildren(AstVisitor visitor);

  public Location location() {
    return location;
  }

  protected void acceptSafe(AstNode node, AstVisitor visitor) {
    if (node != null) {
      node.accept(visitor);
    }
  }
}
