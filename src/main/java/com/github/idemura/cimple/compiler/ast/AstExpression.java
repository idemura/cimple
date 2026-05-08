package com.github.idemura.cimple.compiler.ast;

public abstract sealed class AstExpression extends AstNode
    permits AstCall,
        AstArrayAccess,
        AstReceiverLookup,
        AstFieldAccess,
        AstLiteral,
        AstEntityRef,
        AstCast {
  protected AstExpression() {}

  public abstract AstTypeRef getType();
}
