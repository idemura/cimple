package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.Location;

public abstract class AstNode {
  private Location location;

  public abstract void accept(AstVisitor visitor);

  public abstract void acceptChildren(AstVisitor visitor);

  public Location location() {
    return location;
  }

  public void location(Location location) {
    this.location = location;
  }

  protected void acceptSafe(AstNode node, AstVisitor visitor) {
    if (node != null) {
      node.accept(visitor);
    }
  }
}
