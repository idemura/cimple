package com.github.idemura.cimple.compiler;

public class AstNameRef extends AstExpression {
  private final String name;
  // Variable definition that it refers to
  private VariableDef variable;

  AstNameRef(Location location, String name) {
    super(location);
    this.name = name;
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
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
