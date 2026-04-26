package com.github.idemura.cimple.compiler;

public class AstVariable extends AstStatement {
  private boolean mutable;
  private String name;
  private TypeRef typeRef;
  private AstExpression init;

  AstVariable() {}

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  boolean getMutable() {
    return mutable;
  }

  void setMutable(boolean mutable) {
    this.mutable = mutable;
  }

  void setName(String name) {
    this.name = name;
  }

  String getName() {
    return name;
  }

  void setTypeRef(TypeRef typeRef) {
    this.typeRef = typeRef;
  }

  TypeRef getTypeRef() {
    return typeRef;
  }

  void setInit(AstExpression init) {
    this.init = init;
  }

  AstExpression getInit() {
    return init;
  }
}
