package com.github.idemura.cimple.compiler;

public class VariableDef {
  private final Location location;
  private final String name;
  private TypeRef typeRef;

  VariableDef(Location location, String name, TypeRef typeRef) {
    this.location = location;
    this.name = name;
    this.typeRef = typeRef;
  }

  @Override
  public String toString() {
    return "VariableDef(" + name + (typeRef == null ? "" : ": " + typeRef) + ")";
  }

  public String getName() {
    return name;
  }

  public Location getLocation() {
    return location;
  }

  public TypeRef getTypeRef() {
    return typeRef;
  }

  void setTypeRef(TypeRef typeRef) {
    this.typeRef = typeRef;
  }
}
