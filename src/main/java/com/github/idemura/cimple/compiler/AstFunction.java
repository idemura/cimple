package com.github.idemura.cimple.compiler;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

public class AstFunction extends AstNode {
  private final String name;
  private TypeRef resultType;
  private final List<VariableDef> parameters;
  private final AstBlock block;

  static AstFunction std(String name, TypeRef resultTypeRef, List<TypeRef> parameters) {
    return new AstFunction(
        null,
        name,
        resultTypeRef,
        parameters.stream().map(t -> new VariableDef(null, "_", t)).toList(),
        null);
  }

  AstFunction(
      Location location,
      String name,
      TypeRef resultType,
      List<VariableDef> parameters,
      AstBlock block) {
    super(location);
    this.name = name;
    this.resultType = checkNotNull(resultType);
    this.parameters = parameters;
    this.block = block;
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    return (o instanceof AstFunction other)
        && name.equals(other.name)
        && resultType.equals(other.resultType)
        && getParameterTypes().equals(other.getParameterTypes());
  }

  @Override
  public String toString() {
    return "FUNCTION %s(%s): %s".formatted(name, parameters, resultType);
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  public String getName() {
    return name;
  }

  public TypeRef getResultType() {
    return resultType;
  }

  void setResultType(TypeRef resultType) {
    this.resultType = resultType;
  }

  public List<VariableDef> getParameters() {
    return parameters;
  }

  public List<TypeRef> getParameterTypes() {
    return parameters.stream().map(VariableDef::getTypeRef).toList();
  }

  public AstBlock getBlock() {
    return block;
  }

  public TypeRef getOverloadType() {
    return parameters.isEmpty() ? null : parameters.getLast().getTypeRef();
  }
}
