package com.github.idemura.cimple.compiler.ast;

public final class AstFieldAccess extends AstExpression {
  private AstExpression object;
  private String fieldName;
  private boolean method;
  private AstVariable field;

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {
    object.accept(visitor);
  }

  @Override
  public AstExpression rewrite(AstExpressionRewriter rewriter) {
    object = object.rewrite(rewriter);
    return rewriter.rewrite(this);
  }

  @Override
  public AstType type() {
    if (method) {
      return object.type();
    }
    return field == null ? null : field.type();
  }

  public AstExpression object() {
    return object;
  }

  public void object(AstExpression object) {
    this.object = object;
  }

  public String fieldName() {
    return fieldName;
  }

  public void fieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public AstVariable field() {
    return field;
  }

  public void field(AstVariable field) {
    this.field = field;
  }

  public boolean method() {
    return method;
  }

  public void method(boolean method) {
    this.method = method;
  }
}
