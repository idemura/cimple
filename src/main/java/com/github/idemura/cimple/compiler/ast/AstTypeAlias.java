package com.github.idemura.cimple.compiler.ast;

public final class AstTypeAlias extends AstType {
  private QualifiedName name;
  private TypeRef baseTypeRef;

  public AstTypeAlias() {}

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  @Override
  public QualifiedName getName() {
    return name;
  }

  public void setName(QualifiedName name) {
    this.name = name;
  }

  public TypeRef getBaseTypeRef() {
    return baseTypeRef;
  }

  public void setBaseTypeRef(TypeRef baseTypeRef) {
    this.baseTypeRef = baseTypeRef;
  }
}
