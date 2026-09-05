package io.lang.cimple.compiler;

import com.google.common.collect.ImmutableList;
import java.util.List;

public final class AstInterfaceType extends AstType {
  private final Identifier name;
  private final ImmutableList<AstFunction> functions;

  public AstInterfaceType(Identifier name, ImmutableList<AstFunction> functions) {
    super(name.location());
    this.name = name;
    this.functions = functions;
  }

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {
    for (var function : functions) {
      function.accept(visitor);
    }
  }

  @Override
  public Identifier name() {
    return name;
  }

  public List<AstFunction> functions() {
    return functions;
  }
}
