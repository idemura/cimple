package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.Location;

public abstract class AstNode {
  protected Location location;

  public abstract Object accept(AstVisitor visitor);

  public Location getLocation() {
    return location;
  }

  public void setLocation(Location location) {
    this.location = location;
  }
}
