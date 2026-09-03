package io.lang.cimple.compiler;

import com.google.common.collect.ImmutableList;

public final class AstStructType extends AstType {
  private final Identifier name;
  private final ImmutableList<AstVariable> fields;

  public AstStructType(Identifier name, ImmutableList<AstVariable> fields) {
    this.name = name;
    this.fields = fields;
  }

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {
    for (var field : fields) {
      field.accept(visitor);
    }
  }

  @Override
  public Identifier name() {
    return name;
  }

  public ImmutableList<AstVariable> fields() {
    return fields;
  }
}
