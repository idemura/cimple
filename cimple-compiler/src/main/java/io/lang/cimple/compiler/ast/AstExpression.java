package io.lang.cimple.compiler.ast;

public abstract sealed class AstExpression extends AstNode
    permits AstAssign,
        AstCompoundAssign,
        AstEntityRef,
        AstNew,
        AstCall,
        AstFunctionPointerCall,
        AstArrayAccess,
        AstFieldAccess,
        AstLiteral,
        AstCast {
  protected AstExpression() {}

  public abstract AstType type();

  public abstract AstExpression rewrite(AstExpressionRewriteVisitor visitor);
}
