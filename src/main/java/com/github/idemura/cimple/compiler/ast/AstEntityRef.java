package com.github.idemura.cimple.compiler.ast;

import static com.google.common.base.Preconditions.checkNotNull;

import com.github.idemura.cimple.compiler.Identifier;
import java.util.Objects;

public final class AstEntityRef extends AstExpression {
  private Identifier name;
  private AstEntity entity;

  // Test helper.
  public static AstEntityRef ofName(String name) {
    return ofName(null, name);
  }

  public static AstEntityRef ofName(String moduleName, String name) {
    var ref = new AstEntityRef();
    ref.name(Identifier.ofEntity(name).withModule(moduleName));
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
  public AstTypeRef typeRef() {
    return switch (entity) {
      case AstVariable variable -> variable.typeRef();
      case AstFunction function -> function.typeRef();
    };
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

  public boolean isBuiltin() {
    return name.isBuiltin();
  }
}
