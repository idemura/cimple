package com.github.idemura.cimple.compiler;

import java.util.ArrayList;
import java.util.List;

public class AstModule extends AstAbstractNode {
  private String name;
  private List<AstFunction> functions = new ArrayList<>();
  private List<AstAbstractType> types = new ArrayList<>();
  private List<AstVariable> variables = new ArrayList<>();

  AstModule() {
    super();
  }

  void setName(String name) {
    this.name = name;
  }

  String getName() {
    return name;
  }

  void addFunction(AstFunction f) {
    functions.add(f);
  }

  void addType(AstAbstractType type) {
    types.add(type);
  }

  void addVariable(AstVariable v) {
    variables.add(v);
  }

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  List<AstFunction> getFunctions() {
    return List.copyOf(functions);
  }

  List<AstAbstractType> getTypes() {
    return List.copyOf(types);
  }

  List<AstVariable> getVariables() {
    return List.copyOf(variables);
  }
}
