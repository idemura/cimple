package com.github.idemura.cimple.compiler.ast;

public abstract sealed class AstExpression extends AstNode
    permits AstCall,
        AstAssign,
        AstCompoundAssign,
        AstEntityRef,
        AstArrayAccess,
        AstFieldAccess,
        AstNew,
        AstLiteral,
        AstCast {
  protected AstExpression() {}

  public abstract AstType type();

  public abstract AstExpression rewrite(AstExpressionRewriteVisitor visitor);
}
