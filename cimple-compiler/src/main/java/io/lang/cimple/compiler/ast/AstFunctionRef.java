package io.lang.cimple.compiler.ast;

import static com.google.common.base.Preconditions.checkNotNull;

import io.lang.cimple.compiler.Identifier;
import io.lang.cimple.compiler.Location;

public final class AstFunctionRef extends AstEntityRef {
  private AstFunction function;

  public AstFunctionRef(Location location, Identifier name) {
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
    return "FUNCTION_REF(%s)".formatted(name());
  }

  @Override
  public AstType type() {
    return function == null ? null : function.type();
  }

  public AstFunction function() {
    return function;
  }

  public void function(AstFunction function) {
    this.function = checkNotNull(function);
  }

  public boolean isResolved() {
    return function != null;
  }
}
