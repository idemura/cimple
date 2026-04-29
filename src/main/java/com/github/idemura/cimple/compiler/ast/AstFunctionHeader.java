package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.Location;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;

public final class AstFunctionHeader {
  private Location location;
  private String boundTypeName;
  private QualifiedName name;
  private TypeRef resultType;
  private List<AstVariable> parameters;

  public AstFunctionHeader() {}

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public boolean equals(Object object) {
    return this == object
        || (object instanceof AstFunctionHeader other
            && Objects.equals(name, other.name)
            && Objects.equals(parameters, other.parameters)
            && Objects.equals(resultType, other.resultType));
  }

  public Location getLocation() {
    return location;
  }

  public void setLocation(Location location) {
    this.location = location;
  }

  public String getBoundTypeName() {
    return boundTypeName;
  }

  public void setBoundTypeName(String boundTypeName) {
    this.boundTypeName = boundTypeName;
  }

  public QualifiedName getName() {
    return name;
  }

  public void setName(QualifiedName name) {
    this.name = name;
  }

  public TypeRef getResultType() {
    return resultType;
  }

  public void setResultType(TypeRef resultType) {
    this.resultType = resultType;
  }

  public List<AstVariable> getParameters() {
    return ImmutableList.copyOf(parameters);
  }

  public void setParameters(List<AstVariable> parameters) {
    this.parameters = ImmutableList.copyOf(parameters);
  }
}
