package com.github.idemura.cimple.compiler;

import java.util.Objects;

public class VariableDef {
  private Location location;
  private String name;
  private TypeRef typeRef;

  public VariableDef(String name, TypeRef typeRef) {
    this.name = name;
    this.typeRef = typeRef;
  }

  public VariableDef() {}

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

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setTypeRef(TypeRef typeRef) {
    this.typeRef = typeRef;
  }

  public TypeRef getTypeRef() {
    return typeRef;
  }

  public void setLocation(Location location) {
    this.location = location;
  }

  public Location getLocation() {
    return location;
  }
}
