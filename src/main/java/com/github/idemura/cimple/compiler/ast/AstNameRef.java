package com.github.idemura.cimple.compiler.ast;

import java.util.Objects;

public final class AstNameRef extends AstExpression {
  private QualifiedName name;
  private AstVariable variable;

  public AstNameRef(String name) {
    this.name = new QualifiedName(name);
  }

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
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
  public String toString() {
    return "AstNameRef(%s)".formatted(name);
  }

  @Override
  public AstType getType() {
    return variable.getTypeRef().getType();
  }

  public QualifiedName getName() {
    return name;
  }

  public void setName(QualifiedName name) {
    this.name = name;
  }

  public void setVariable(AstVariable variable) {
    this.variable = variable;
  }

  public AstVariable getVariable() {
    return variable;
  }
}
