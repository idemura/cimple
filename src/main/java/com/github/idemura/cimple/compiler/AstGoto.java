package com.github.idemura.cimple.compiler;

import java.util.Objects;

public class AstGoto extends AstAbstractStatement {
  private String label;

  AstGoto() {}

  AstGoto(String label) {
    this.label = label;
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
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

  void setLabel(String label) {
    this.label = label;
  }

  String getLabel() {
    return label;
  }
}
