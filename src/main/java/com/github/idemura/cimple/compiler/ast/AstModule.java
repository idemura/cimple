package com.github.idemura.cimple.compiler.ast;

import java.util.ArrayList;
import java.util.List;

public final class AstModule extends AstNode {
  private String name;
  private List<AstFunction> functions = new ArrayList<>();
  private List<AstType> types = new ArrayList<>();
  private List<AstVariable> variables = new ArrayList<>();

  public AstModule() {
    super();
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void addFunction(AstFunction f) {
    functions.add(f);
  }

  public void addType(AstType type) {
    types.add(type);
  }

  public void addVariable(AstVariable v) {
    variables.add(v);
  }

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  public List<AstFunction> getFunctions() {
    return List.copyOf(functions);
  }

  public List<AstType> getTypes() {
    return List.copyOf(types);
  }

  public List<AstVariable> getVariables() {
    return List.copyOf(variables);
  }
}
