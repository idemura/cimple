package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.QualifiedName;
import java.util.Objects;

public final class AstTypeRef extends AstNode {
  private QualifiedName name;
  private AstType type;

  // Test helper.
  public static AstTypeRef ofName(String name) {
    return ofName(null, name);
  }

  public static AstTypeRef ofName(String moduleName, String name) {
    var ref = new AstTypeRef();
    ref.name(QualifiedName.ofType(name).withModule(moduleName));
    return ref;
  }

  public static AstTypeRef ofType(AstType type) {
    var ref = new AstTypeRef();
    ref.name(type.name());
    ref.type(type);
    ref.markResolved();
    return ref;
  }

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
        || (object instanceof AstTypeRef other && Objects.equals(name, other.name));
  }

  @Override
  public String toString() {
    return "TYPE_REF(%s)".formatted(name);
  }

  public QualifiedName name() {
    return name;
  }

  public void name(QualifiedName name) {
    this.name = name;
  }

  public AstType type() {
    return type;
  }

  public void type(AstType type) {
    this.type = type;
  }
}
