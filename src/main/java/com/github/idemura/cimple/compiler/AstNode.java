package com.github.idemura.cimple.compiler;

abstract class AstNode implements VisitorNode {
  private final Location location;

  AstNode(Location location) {
    this.location = location;
  }

  Location getLocation() {
    return location;
  }
}
