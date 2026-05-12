package com.github.idemura.cimple.compiler.ast;

import static com.google.common.base.Preconditions.checkNotNull;

import com.github.idemura.cimple.compiler.Identifier;
import java.util.Objects;

public final class AstEntityRef extends AstExpression {
  private Identifier name;
  private AstEntity entity;

  public AstEntityRef() {}

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  @Override
  public AstExpression acceptRewriter(AstExpressionRewriter rewriter) {
    return rewriter.rewrite(this);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public boolean equals(Object object) {
    return this == object
        || (object instanceof AstEntityRef other && Objects.equals(name, other.name));
  }

  @Override
  public String toString() {
    return "ENTITY_REF(%s)".formatted(name);
  }

  @Override
  public AstType type() {
    return entity.type();
  }

  public Identifier name() {
    return name;
  }

  public void name(Identifier name) {
    this.name = name;
  }

  public AstEntity entity() {
    return entity;
  }

  public void entity(AstEntity entity) {
    this.entity = checkNotNull(entity);
  }

  public boolean isResolved() {
    return entity != null;
  }

  public boolean isBuiltin() {
    return name.isBuiltin();
  }
}
