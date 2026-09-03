package io.lang.cimple.compiler;

import static com.google.common.base.Preconditions.checkNotNull;

public final class AstVariableRef extends AstEntityRef {
  private AstVariable variable;

  public AstVariableRef(Identifier name) {
    super(name);
  }

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public AstExpression rewrite(AstExpressionRewriteVisitor visitor) {
    return visitor.rewrite(this);
  }

  @Override
  public String toString() {
    return "VARIABLE_REF(%s)".formatted(name());
  }

  @Override
  public AstType type() {
    return variable == null ? null : variable.type();
  }

  public AstVariable variable() {
    return variable;
  }

  public void variable(AstVariable variable) {
    this.variable = checkNotNull(variable);
  }

  public boolean isResolved() {
    return variable != null;
  }
}
