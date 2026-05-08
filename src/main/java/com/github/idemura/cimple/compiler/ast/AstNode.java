package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.Location;

public abstract class AstNode {
  private Location location;
  private boolean nameResolved = false;

  public abstract Object accept(AstVisitor visitor);

  public Location location() {
    return location;
  }

  public void location(Location location) {
    this.location = location;
  }

  public boolean isNameResolved() {
    return nameResolved;
  }

  public void markNameResolved() {
    nameResolved = true;
  }
}
