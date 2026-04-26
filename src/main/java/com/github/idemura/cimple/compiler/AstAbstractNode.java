package com.github.idemura.cimple.compiler;

public abstract class AstAbstractNode {
  protected Location location;

  public abstract void accept(AstVisitor visitor);

  void setLocation(Location location) {
    this.location = location;
  }

  Location getLocation() {
    return location;
  }
}
