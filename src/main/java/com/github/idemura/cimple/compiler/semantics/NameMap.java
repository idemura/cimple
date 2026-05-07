package com.github.idemura.cimple.compiler.semantics;

import com.github.idemura.cimple.compiler.ast.AstEntity;
import com.github.idemura.cimple.compiler.ast.AstFunction;
import com.github.idemura.cimple.compiler.ast.AstType;
import com.github.idemura.cimple.compiler.ast.AstVariable;
import java.util.HashMap;
import java.util.Map;

public class NameMap {
  private final Map<String, AstType> typesByName = new HashMap<>();
  private final Map<String, AstEntity> entitiesByName = new HashMap<>();

  public NameMap() {}

  public AstType addType(AstType type) {
    return typesByName.putIfAbsent(type.getName().name(), type);
  }

  public AstEntity addFunction(AstFunction function) {
    return entitiesByName.putIfAbsent(function.getHeader().getName().name(), function);
  }

  public AstEntity addVariable(AstVariable variable) {
    return entitiesByName.putIfAbsent(variable.getName().name(), variable);
  }

  public AstType lookupType(String name) {
    return typesByName.get(name);
  }

  public AstEntity lookupEntity(String name) {
    return entitiesByName.get(name);
  }
}
