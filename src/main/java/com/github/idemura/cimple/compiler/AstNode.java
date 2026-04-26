package com.github.idemura.cimple.compiler;

abstract class AstNode implements VisitorNode {
  protected Location location;

  void setLocation(Location location) {
    this.location = location;
  }

  Location getLocation() {
    return location;
  }
}
