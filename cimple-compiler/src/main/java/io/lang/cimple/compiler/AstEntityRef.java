package io.lang.cimple.compiler;

import java.util.Objects;

public abstract sealed class AstEntityRef extends AstExpression
    permits AstFunctionRef, AstVariableRef {
  private final Identifier name;

  protected AstEntityRef(Identifier name) {
    super(name.location());
    this.name = name;
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {}

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public boolean equals(Object object) {
    return this == object
        || (object instanceof AstEntityRef other && Objects.equals(name, other.name));
  }

  public Identifier name() {
    return name;
  }

  public boolean isBuiltin() {
    return name.isBuiltin();
  }
}
