package io.lang.cimple.compiler.ast;

import java.util.ArrayList;
import java.util.List;

public final class AstModule extends AstNode {
  private final List<AstNode> definitions = new ArrayList<>();
  private String name;
  private boolean builtin;

  public AstModule() {
    super();
  }

  public String name() {
    return name;
  }

  public void name(String name) {
    this.name = name;
  }

  public boolean builtin() {
    return builtin;
  }

  public void builtin(boolean builtin) {
    this.builtin = builtin;
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

  public List<AstNode> definitions() {
    return definitions;
  }

  public AstType findType(String name) {
    for (var definition : definitions) {
      if (definition instanceof AstType type && name.equals(type.name().type())) {
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
        var objectType = function.header().objectType();
        if (objectType == null) {
          return function;
        }
      }
    }
    return null;
  }

  public AstFunction findMethod(String objectTypeName, String name) {
    for (var definition : definitions) {
      if (definition instanceof AstFunction function && function.name().entity().equals(name)) {
        var objectType = function.header().objectType();
        if (objectType != null && objectTypeName.equals(objectType.name().type())) {
          return function;
        }
      }
    }
    return null;
  }
}
