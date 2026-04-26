package com.github.idemura.cimple.compiler;

import java.util.ArrayList;
import java.util.List;

public class AstFunction extends AstNode {
  private String boundTypeName;
  private String name;
  private TypeRef resultType;
  private List<VariableDef> parameters = new ArrayList<>();
  private AstBlock block;

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    return (o instanceof AstFunction other) && name.equals(other.name);
  }

  @Override
  public String toString() {
    return "FUNCTION %s(%s): %s".formatted(name, parameters, resultType);
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  void setBoundTypeName(String boundTypeName) {
    this.boundTypeName = boundTypeName;
  }

  String getBoundTypeName() {
    return boundTypeName;
  }

  void setResultType(TypeRef resultType) {
    this.resultType = resultType;
  }

  public TypeRef getResultType() {
    return resultType;
  }

  void addParameter(VariableDef parameter) {
    this.parameters.add(parameter);
  }

  public List<VariableDef> getParameters() {
    return parameters;
  }

  void setBlock(AstBlock block) {
    this.block = block;
  }

  public AstBlock getBlock() {
    return block;
  }
}
