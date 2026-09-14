package io.lang.cimple.compiler;

import java.util.Map;
import java.util.Objects;

public final class AstTypeWildcard extends AstType {
  private final Identifier name;

  public AstTypeWildcard(Identifier name) {
    super(name.location());
    this.name = name;
  }

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
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
        || (object instanceof AstTypeWildcard other && Objects.equals(name, other.name));
  }

  @Override
  public Identifier name() {
    return name;
  }

  @Override
  public boolean containsWildcard() {
    return true;
  }

  @Override
  public AstType substitute(Map<AstTypeWildcard, AstType> substitutions) {
    return substitutions.getOrDefault(this, this);
  }
}
