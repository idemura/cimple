package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.Location;
import com.github.idemura.cimple.compiler.QualifiedName;

public final class AstFunction extends AstEntity {
  private AstFunctionHeader header;
  private AstBlock block;

  public QualifiedName name() {
    return header.name();
  }

  public void name(QualifiedName name) {
    header.name(name);
  }

  @Override
  public Location location() {
    return header.location();
  }

  @Override
  public void location(Location location) {
    header.location(location);
  }

  @Override
  public int hashCode() {
    return header.name().hashCode();
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    return (object instanceof AstFunction other) && header.name().equals(other.header().name());
  }

  @Override
  public String toString() {
    return "FUNCTION %s(%s): %s".formatted(header.name(), header.parameters(), header.resultType());
  }

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  public AstFunctionHeader header() {
    return header;
  }

  public void header(AstFunctionHeader header) {
    this.header = header;
  }

  public AstBlock block() {
    return block;
  }

  public void block(AstBlock block) {
    this.block = block;
  }
}
