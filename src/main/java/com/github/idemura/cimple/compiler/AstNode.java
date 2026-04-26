package com.github.idemura.cimple.compiler;

public abstract class AstNode {
  protected Location location;

  public abstract void accept(Visitor visitor);

  void setLocation(Location location) {
    this.location = location;
  }

  Location getLocation() {
    return location;
  }
}
