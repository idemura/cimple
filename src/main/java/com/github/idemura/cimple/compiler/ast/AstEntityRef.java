package com.github.idemura.cimple.compiler.ast;

import static java.util.Objects.requireNonNull;

import com.github.idemura.cimple.compiler.QualifiedName;
import java.util.Objects;

public final class AstEntityRef extends AstExpression {
  private QualifiedName name;
  private AstEntity entity;

  // For testing
  public static AstEntityRef ofName(String name) {
    return ofName(null, name);
  }

  public static AstEntityRef ofName(String moduleName, String name) {
    var ref = new AstEntityRef();
    ref.setName(new QualifiedName(moduleName, name));
    return ref;
  }

  public AstEntityRef() {}

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
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
  public AstTypeRef getType() {
    requireNonNull(entity);
    return switch (entity) {
      case AstVariable variable -> variable.getType();
      case AstFunction function -> function.getHeader().getResultType();
    };
  }

  public QualifiedName getName() {
    return name;
  }

  public void setName(QualifiedName name) {
    this.name = name;
  }

  public AstEntity getEntity() {
    return entity;
  }

  public void setEntity(AstEntity entity) {
    this.entity = requireNonNull(entity);
  }

  public boolean isBuiltin() {
    return name.isBuiltin();
  }
}
