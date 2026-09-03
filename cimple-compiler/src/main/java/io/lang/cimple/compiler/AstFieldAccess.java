package io.lang.cimple.compiler;

public final class AstFieldAccess extends AstExpression {
  private AstExpression object;
  private String fieldName;
  private AstVariable field;

  public AstFieldAccess(Location location, AstExpression object, String fieldName) {
    super(location);
    this.object = object;
    this.fieldName = fieldName;
  }

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {
    object.accept(visitor);
  }

  @Override
  public AstExpression rewrite(AstExpressionRewriteVisitor visitor) {
    object = object.rewrite(visitor);
    return visitor.rewrite(this);
  }

  @Override
  public AstType type() {
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

  public AstVariable field() {
    return field;
  }

  public void field(AstVariable field) {
    this.field = field;
  }
}
