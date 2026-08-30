package io.lang.cimple.compiler.ast;

import static com.google.common.base.Preconditions.checkNotNull;

import io.lang.cimple.compiler.Identifier;
import io.lang.cimple.compiler.Location;

public final class AstVariableRef extends AstEntityRef {
  private AstVariable variable;

  public AstVariableRef(Location location, Identifier name) {
    super(location, name);
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
