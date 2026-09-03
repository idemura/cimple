package io.lang.cimple.compiler;

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
  protected AstExpression(Location location) {
    super(location);
  }

  public abstract AstType type();

  public abstract AstExpression rewrite(AstExpressionRewriteVisitor visitor);
}
