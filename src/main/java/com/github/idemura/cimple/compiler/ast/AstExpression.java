package com.github.idemura.cimple.compiler.ast;

public abstract sealed class AstExpression extends AstNode
    permits AstCall,
        AstEntityRef,
        AstArrayAccess,
        AstFieldAccess,
        AstReceiverLookup,
        AstNew,
        AstLiteral,
        AstCast {
  protected AstExpression() {}

  public abstract AstType type();

  public abstract AstExpression rewrite(AstExpressionRewriter rewriter);
}
