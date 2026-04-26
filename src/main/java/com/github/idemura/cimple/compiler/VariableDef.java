package com.github.idemura.cimple.compiler;

public class VariableDef {
  private Location location;
  private String name;
  private TypeRef typeRef;

  VariableDef() {}

  @Override
  public String toString() {
    return "VariableDef(" + name + (typeRef == null ? "" : ": " + typeRef) + ")";
  }

  void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  void setTypeRef(TypeRef typeRef) {
    this.typeRef = typeRef;
  }

  public TypeRef getTypeRef() {
    return typeRef;
  }

  void setLocation(Location location) {
    this.location = location;
  }

  Location getLocation() {
    return location;
  }
}
