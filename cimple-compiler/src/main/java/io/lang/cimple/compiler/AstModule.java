package io.lang.cimple.compiler;

import com.google.common.collect.ImmutableList;

public final class AstModule extends AstNode {
  private final Identifier name;
  private final ImmutableList<AstNode> definitions;

  public AstModule(Identifier name, ImmutableList<AstNode> definitions) {
    this.name = name;
    this.definitions = definitions;
  }

  public Identifier name() {
    return name;
  }

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {
    for (var definition : definitions) {
      definition.accept(visitor);
    }
  }

  public ImmutableList<AstNode> definitions() {
    return definitions;
  }

  public AstType findType(String name) {
    for (var definition : definitions) {
      if (definition instanceof AstType type && name.equals(type.name().entity())) {
        return type;
      }
    }
    return null;
  }

  public AstVariable findVariable(String name) {
    for (var definition : definitions) {
      if (definition instanceof AstVariable variable && name.equals(variable.name().entity())) {
        return variable;
      }
    }
    return null;
  }

  public AstFunction findFunction(String name) {
    for (var definition : definitions) {
      if (definition instanceof AstFunction function && name.equals(function.name().entity())) {
        return function;
      }
    }
    return null;
  }
}
