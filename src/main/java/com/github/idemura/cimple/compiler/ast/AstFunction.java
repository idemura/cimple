package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.Location;
import com.github.idemura.cimple.compiler.QualifiedName;

public final class AstFunction extends AstEntity {
  private QualifiedName name;
  private AstFunctionHeader header;
  private AstBlock block;
  private AstTypeRef typeRef;

  public QualifiedName name() {
    return name;
  }

  public void name(QualifiedName name) {
    this.name = name;
  }

  @Override
  public Location location() {
    return header.location();
  }

  @Override
  public void location(Location location) {
    header.location(location);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    return (object instanceof AstFunction other) && name.equals(other.name());
  }

  @Override
  public String toString() {
    return "FUNCTION %s(%s): %s".formatted(name, header.parameters(), header.resultType());
  }

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  public AstFunctionHeader header() {
    return header;
  }

  public void header(AstFunctionHeader header) {
    this.header = header;
  }

  public void makeLambdaType() {
    var typeName = new QualifiedName("_lambda");
    var type = new AstFunctionType();
    type.name(typeName);
    type.header(header);
    typeRef = new AstTypeRef();
    typeRef.name(typeName);
    typeRef.type(type);
  }

  public AstTypeRef typeRef() {
    return typeRef;
  }

  public AstBlock block() {
    return block;
  }

  public void block(AstBlock block) {
    this.block = block;
  }
}
