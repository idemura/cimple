package com.github.idemura.cimple.compiler.ast;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;

public final class AstFunctionHeader extends AstNode {
  private AstTypeRef receiverType;
  private int receiverIndex = -1;
  private List<AstVariable> parameters;
  private AstTypeRef resultType;

  public AstFunctionHeader() {}

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  @Override
  public int hashCode() {
    return Objects.hash(receiverType, parameters, resultType);
  }

  @Override
  public boolean equals(Object object) {
    return this == object
        || (object instanceof AstFunctionHeader other
            && parameterListsEqual(parameters, other.parameters)
            && Objects.equals(resultType, other.resultType));
  }

  public static boolean parameterListsEqual(List<AstVariable> a, List<AstVariable> b) {
    if (a.size() != b.size()) {
      return false;
    }
    for (var i = 0; i < a.size(); i++) {
      if (!Objects.equals(a.get(i).typeRef(), b.get(i).typeRef())) {
        return false;
      }
    }
    return true;
  }

  public AstTypeRef receiverType() {
    return receiverType;
  }

  public void receiverType(AstTypeRef receiverType) {
    this.receiverType = receiverType;
  }

  public int receiverIndex() {
    return receiverIndex;
  }

  public void receiverIndex(int receiverIndex) {
    this.receiverIndex = receiverIndex;
  }

  public List<AstVariable> parameters() {
    return parameters;
  }

  public void parameters(List<AstVariable> parameters) {
    this.parameters = ImmutableList.copyOf(parameters);
  }

  public AstTypeRef resultType() {
    return resultType;
  }

  public void resultType(AstTypeRef resultType) {
    this.resultType = resultType;
  }
}
