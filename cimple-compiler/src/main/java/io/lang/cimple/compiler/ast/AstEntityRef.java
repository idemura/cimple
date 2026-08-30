package io.lang.cimple.compiler.ast;

import static com.google.common.base.Preconditions.checkNotNull;

import io.lang.cimple.compiler.Identifier;
import io.lang.cimple.compiler.Location;
import java.util.Objects;

public abstract sealed class AstEntityRef extends AstExpression
    permits AstFunctionRef, AstVariableRef {
  private final Identifier name;

  protected AstEntityRef(Location location, Identifier name) {
    this.name = checkNotNull(name);
    location(location);
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
        || (object != null
            && object.getClass() == getClass()
            && Objects.equals(name, ((AstEntityRef) object).name));
  }

  public Identifier name() {
    return name;
  }

  public boolean isBuiltin() {
    return name.isBuiltin();
  }
}
