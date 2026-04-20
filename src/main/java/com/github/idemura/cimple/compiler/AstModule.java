package com.github.idemura.cimple.compiler;

import java.util.ArrayList;
import java.util.List;

public class AstModule extends AstNode {
  private final List<AstFunction> functions = new ArrayList<>();
  private final List<AstVariable> variables = new ArrayList<>();

  AstModule() {
    super(null);
  }

  void addFunction(AstFunction f) {
    functions.add(f);
  }

  void addVariable(AstVariable v) {
    variables.add(v);
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  List<AstFunction> getFunctions() {
    return List.copyOf(functions);
  }

  List<AstVariable> getVariables() {
    return List.copyOf(variables);
  }
}
