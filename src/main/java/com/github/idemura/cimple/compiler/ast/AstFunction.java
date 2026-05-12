package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.Identifier;
import com.github.idemura.cimple.compiler.Location;

public final class AstFunction extends AstEntity {
  private Identifier name;
  private AstFunctionHeader header;
  private AstBlock block;
  private AstTypeHolder type;

  @Override
  public Identifier name() {
    return name;
  }

  @Override
  public void name(Identifier name) {
    this.name = name;
  }

  public void makeLambdaType() {
    var typeName = Identifier.ofEntity("_lambda").builtin();
    var type = new AstFunctionType();
    type.name(typeName);
    type.header(header);
    this.type = AstTypeHolder.of(type);
  }

  @Override
  public AstType type() {
    return type == null ? null : type.type();
  }

  public void type(AstType type) {
    this.type = AstTypeHolder.of(type);
  }

  public AstTypeHolder typeHolder() {
    return type;
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
    return "FUNCTION %s".formatted(name);
  }

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {
    header.accept(visitor);
    acceptSafe(type, visitor);
    block.accept(visitor);
  }

  public AstFunctionHeader header() {
    return header;
  }

  public void header(AstFunctionHeader header) {
    this.header = header;
  }

  public AstBlock block() {
    return block;
  }

  public void block(AstBlock block) {
    this.block = block;
  }
}
