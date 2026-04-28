package com.github.idemura.cimple.compiler.ast;

import java.util.Objects;

public final class AstGoto extends AstStatement {
  private String label;

  public AstGoto() {}

  public AstGoto(String label) {
    this.label = label;
  }

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  @Override
  public int hashCode() {
    return label.hashCode();
  }

  @Override
  public boolean equals(Object object) {
    return this == object
        || (object instanceof AstGoto other && Objects.equals(label, other.label));
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
