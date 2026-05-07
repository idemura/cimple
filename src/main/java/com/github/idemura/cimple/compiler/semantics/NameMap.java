package com.github.idemura.cimple.compiler.semantics;

import com.github.idemura.cimple.compiler.ast.AstFunction;
import com.github.idemura.cimple.compiler.ast.AstType;
import com.github.idemura.cimple.compiler.ast.AstVariable;
import java.util.HashMap;
import java.util.Map;

public class NameMap {
  private final Map<String, AstType> typesByName = new HashMap<>();
  private final Map<String, AstFunction> functionsByName = new HashMap<>();
  private final Map<String, AstVariable> variablesByName = new HashMap<>();

  public NameMap() {}

  public AstType addType(AstType type) {
    return typesByName.putIfAbsent(type.getName().name(), type);
  }

  public AstFunction addFunction(AstFunction function) {
    return functionsByName.putIfAbsent(function.getHeader().getName().name(), function);
  }

  public AstVariable addVariable(AstVariable variable) {
    return variablesByName.putIfAbsent(variable.getName().name(), variable);
  }

  public AstType lookupType(String name) {
    return typesByName.get(name);
  }

  public AstFunction lookupFunction(String name) {
    return functionsByName.get(name);
  }

  public AstVariable lookupVariable(String name) {
    return variablesByName.get(name);
  }
}
