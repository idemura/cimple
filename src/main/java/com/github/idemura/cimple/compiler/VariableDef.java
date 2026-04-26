package com.github.idemura.cimple.compiler;

import java.util.Objects;

public class VariableDef {
  private Location location;
  private String name;
  private TypeRef typeRef;

  VariableDef(String name, TypeRef typeRef) {
    this.name = name;
    this.typeRef = typeRef;
  }

  VariableDef() {}

  @Override
  public String toString() {
    return "VariableDef(" + name + (typeRef == null ? "" : " " + typeRef) + ")";
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public boolean equals(Object object) {
    return this == object
        || (object instanceof VariableDef other
            && Objects.equals(name, other.name)
            && Objects.equals(typeRef, other.typeRef));
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
