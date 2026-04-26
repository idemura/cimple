package com.github.idemura.cimple.compiler;

import java.util.Objects;

public class AstNameRef extends AstAbstractExpression {
  private String name;
  // Variable definition that it refers to
  private VariableDef variable;

  AstNameRef(String name) {
    this.name = name;
  }

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public boolean equals(Object object) {
    return this == object
        || (object instanceof AstNameRef other && Objects.equals(name, other.name));
  }

  @Override
  TypeRef getTypeRef() {
    return variable.getTypeRef();
  }

  String getName() {
    return name;
  }

  void setVariable(VariableDef variable) {
    this.variable = variable;
  }

  VariableDef getVariable() {
    return variable;
  }
}
