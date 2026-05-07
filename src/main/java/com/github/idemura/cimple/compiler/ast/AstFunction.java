package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.Location;
import com.github.idemura.cimple.compiler.QualifiedName;

public final class AstFunction extends AstEntity {
  private AstFunctionHeader header;
  private AstBlock block;

  public QualifiedName getName() {
    return header.getName();
  }

  public void setName(QualifiedName name) {
    header.setName(name);
  }

  @Override
  public Location getLocation() {
    return header.getLocation();
  }

  @Override
  public void setLocation(Location location) {
    header.setLocation(location);
  }

  @Override
  public int hashCode() {
    return header.getName().hashCode();
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    return (object instanceof AstFunction other)
        && header.getName().equals(other.getHeader().getName());
  }

  @Override
  public String toString() {
    return "FUNCTION %s(%s): %s"
        .formatted(header.getName(), header.getParameters(), header.getResultType());
  }

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  public AstFunctionHeader getHeader() {
    return header;
  }

  public void setHeader(AstFunctionHeader header) {
    this.header = header;
  }

  public AstBlock getBlock() {
    return block;
  }

  public void setBlock(AstBlock block) {
    this.block = block;
  }
}
