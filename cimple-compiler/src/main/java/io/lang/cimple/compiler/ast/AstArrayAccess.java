package io.lang.cimple.compiler.ast;

public final class AstArrayAccess extends AstExpression {
  private AstExpression array;
  private AstExpression index;

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {
    array.accept(visitor);
    index.accept(visitor);
  }

  @Override
  public AstExpression rewrite(AstExpressionRewriteVisitor visitor) {
    array = array.rewrite(visitor);
    index = index.rewrite(visitor);
    return visitor.rewrite(this);
  }

  @Override
  public AstType type() {
    if (array.type() instanceof AstArrayType arrayType) {
      return arrayType.baseType();
    }
    return AstBuiltinType.VOID;
  }

  public AstExpression array() {
    return array;
  }

  public void array(AstExpression array) {
    this.array = array;
  }

  public AstExpression index() {
    return index;
  }

  public void index(AstExpression index) {
    this.index = index;
  }
}
