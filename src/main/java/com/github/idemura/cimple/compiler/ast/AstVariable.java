package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.TypeRef;

public final class AstVariable extends AstStatement {
  private boolean mutable;
  private String name;
  private TypeRef typeRef;
  private AstExpression init;

  public AstVariable() {}

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  public boolean getMutable() {
    return mutable;
  }

  public void setMutable(boolean mutable) {
    this.mutable = mutable;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setTypeRef(TypeRef typeRef) {
    this.typeRef = typeRef;
  }

  public TypeRef getTypeRef() {
    return typeRef;
  }

  public void setInit(AstExpression init) {
    this.init = init;
  }

  public AstExpression getInit() {
    return init;
  }
}
