package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.Identifier;

public final class AstAliasType extends AstType {
  private Identifier name;
  private final AstTypeHolder targetType = new AstTypeHolder();

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {
    targetType.accept(visitor);
  }

  @Override
  public Identifier name() {
    return name;
  }

  @Override
  public void name(Identifier name) {
    this.name = name;
  }

  public AstType targetType() {
    return targetType.get();
  }

  public void targetType(AstType targetType) {
    this.targetType.set(targetType);
  }
}
