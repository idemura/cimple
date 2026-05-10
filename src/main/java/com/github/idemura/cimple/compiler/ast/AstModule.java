package com.github.idemura.cimple.compiler.ast;

import java.util.ArrayList;
import java.util.List;

public final class AstModule extends AstNode {
  private final List<AstNode> definitions = new ArrayList<>();
  private String name;

  public AstModule() {
    super();
  }

  public String name() {
    return name;
  }

  public void name(String name) {
    this.name = name;
  }

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  public List<AstNode> definitions() {
    return definitions;
  }

  public AstType findType(String name) {
    for (var definition : definitions) {
      if (definition instanceof AstType type && name.equals(type.name().typeName())) {
        return type;
      }
    }
    return null;
  }

  public AstVariable findVariable(String name) {
    for (var definition : definitions) {
      if (definition instanceof AstVariable variable && name.equals(variable.name().entityName())) {
        return variable;
      }
    }
    return null;
  }

  public AstFunction findFunction(String name) {
    for (var definition : definitions) {
      if (definition instanceof AstFunction function && name.equals(function.name().entityName())) {
        var receiverType = function.header().receiverType();
        if (receiverType == null) {
          return function;
        }
      }
    }
    return null;
  }

  public AstFunction findReceiverFunction(String receiverTypeName, String name) {
    for (var definition : definitions) {
      if (definition instanceof AstFunction function && function.name().entityName().equals(name)) {
        var receiverType = function.header().receiverType();
        if (receiverType != null && receiverTypeName.equals(receiverType.name().typeName())) {
          return function;
        }
      }
    }
    return null;
  }
}
