package com.github.idemura.cimple.compiler.ast;

import java.util.ArrayList;
import java.util.List;

public final class AstModule extends AstNode {
  private String name;
  private final List<AstFunction> functions = new ArrayList<>();
  private final List<AstType> types = new ArrayList<>();
  private final List<AstVariable> variables = new ArrayList<>();

  public AstModule() {
    super();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  public List<AstFunction> functions() {
    return functions;
  }

  public List<AstType> types() {
    return types;
  }

  public List<AstVariable> variables() {
    return variables;
  }
}
