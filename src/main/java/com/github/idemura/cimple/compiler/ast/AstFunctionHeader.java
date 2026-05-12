package com.github.idemura.cimple.compiler.ast;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;

public final class AstFunctionHeader extends AstNode {
  private AstTypeHolder receiverType;
  private int receiverIndex = -1;
  private List<AstVariable> parameters;
  private AstTypeHolder resultType;

  public AstFunctionHeader() {}

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {
    // Receiver type ref is also assigned to the receiver parameter during preprocessing.
    for (var parameter : parameters) {
      parameter.accept(visitor);
    }
    acceptSafe(receiverType, visitor);
    acceptSafe(resultType, visitor);
  }

  @Override
  public int hashCode() {
    return Objects.hash(receiverType(), parameters, resultType());
  }

  @Override
  public boolean equals(Object object) {
    return this == object
        || (object instanceof AstFunctionHeader other
            && parameterListsEqual(parameters, other.parameters)
            && Objects.equals(resultType(), other.resultType()));
  }

  public static boolean parameterListsEqual(List<AstVariable> a, List<AstVariable> b) {
    if (a.size() != b.size()) {
      return false;
    }
    for (var i = 0; i < a.size(); i++) {
      if (!Objects.equals(a.get(i).type(), b.get(i).type())) {
        return false;
      }
    }
    return true;
  }

  public AstType receiverType() {
    return receiverType == null ? null : receiverType.value();
  }

  public void receiverType(AstType receiverType) {
    this.receiverType = AstTypeHolder.of(receiverType);
  }

  public AstTypeHolder receiverTypeHolder() {
    return receiverType;
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

  public AstType resultType() {
    return resultType == null ? null : resultType.value();
  }

  public void resultType(AstType resultType) {
    this.resultType = AstTypeHolder.of(resultType);
  }

  public AstTypeHolder resultTypeHolder() {
    return resultType;
  }
}
