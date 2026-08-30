package io.lang.cimple.compiler.ast;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;

public final class AstFunctionHeader extends AstNode {
  private final AstTypeHolder resultType = new AstTypeHolder();
  private List<AstVariable> parameters;

  public AstFunctionHeader() {}

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {
    for (var parameter : parameters) {
      parameter.accept(visitor);
    }
    acceptSafe(resultType, visitor);
  }

  @Override
  public int hashCode() {
    return Objects.hash(parameters, resultType());
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

  public List<AstVariable> parameters() {
    return parameters;
  }

  public void parameters(List<AstVariable> parameters) {
    this.parameters = ImmutableList.copyOf(parameters);
  }

  public AstType resultType() {
    return resultType.get();
  }

  public void resultType(AstType resultType) {
    this.resultType.set(resultType);
  }
}
