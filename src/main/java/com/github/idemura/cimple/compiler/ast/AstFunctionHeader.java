package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.QualifiedName;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;

public final class AstFunctionHeader extends AstNode {
  private QualifiedName name;
  private AstTypeRef objectType;
  private List<AstVariable> parameters;
  private AstTypeRef resultType;

  public AstFunctionHeader() {}

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
        || (object instanceof AstFunctionHeader other
            && Objects.equals(name, other.name)
            && Objects.equals(parameters, other.parameters)
            && Objects.equals(resultType, other.resultType)
            && Objects.equals(objectType, other.objectType));
  }

  public QualifiedName getName() {
    return name;
  }

  public void setName(QualifiedName name) {
    this.name = name;
  }

  public AstTypeRef getObjectType() {
    return objectType;
  }

  public void setObjectType(AstTypeRef objectType) {
    this.objectType = objectType;
  }

  public List<AstVariable> getParameters() {
    return parameters;
  }

  public void setParameters(List<AstVariable> parameters) {
    this.parameters = ImmutableList.copyOf(parameters);
  }

  public AstTypeRef getResultType() {
    return resultType;
  }

  public void setResultType(AstTypeRef resultType) {
    this.resultType = resultType;
  }
}
