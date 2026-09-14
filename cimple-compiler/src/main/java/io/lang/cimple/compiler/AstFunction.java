package io.lang.cimple.compiler;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;

public final class AstFunction extends AstEntity {
  private final Identifier name;
  private final ImmutableList<AstTypeWildcard> wildcards;
  private final ImmutableList<AstVariable> parameters;
  private final AstTypeHolder resultType;
  private final AstBlock block;

  public AstFunction(
      Identifier name,
      ImmutableList<AstTypeWildcard> wildcards,
      ImmutableList<AstVariable> parameters,
      AstType resultType,
      AstBlock block) {
    super(name.location());
    this.name = name;
    this.wildcards = wildcards;
    this.parameters = parameters;
    this.resultType = new AstTypeHolder(resultType);
    this.block = block;
  }

  @Override
  public Identifier name() {
    return name;
  }

  @Override
  public AstFunctionType type() {
    return new AstFunctionType(this);
  }

  public FunctionSignature signature() {
    return new FunctionSignature(
        name().entity(), parameters.stream().map(AstVariable::type).collect(toImmutableList()));
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public boolean equals(Object object) {
    return this == object
        || (object instanceof AstFunction other) && Objects.equals(name, other.name);
  }

  @Override
  public String toString() {
    return "FUNCTION %s".formatted(name);
  }

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {
    for (var wildcard : wildcards) {
      wildcard.accept(visitor);
    }
    for (var parameter : parameters) {
      parameter.accept(visitor);
    }
    resultType.accept(visitor);
    acceptSafe(block, visitor);
  }

  public ImmutableList<AstVariable> parameters() {
    return parameters;
  }

  public ImmutableList<AstTypeWildcard> wildcards() {
    return wildcards;
  }

  public AstType resultType() {
    return resultType.get();
  }

  public void resultType(AstType type) {
    resultType.set(type);
  }

  public AstBlock block() {
    return block;
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
}
