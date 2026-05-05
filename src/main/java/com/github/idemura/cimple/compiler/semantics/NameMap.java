package com.github.idemura.cimple.compiler.semantics;

import com.github.idemura.cimple.compiler.QualifiedName;
import com.github.idemura.cimple.compiler.ast.AstEntity;
import com.github.idemura.cimple.compiler.ast.AstFunction;
import com.github.idemura.cimple.compiler.ast.AstType;
import com.github.idemura.cimple.compiler.ast.AstVariable;
import java.util.HashMap;
import java.util.Map;

public class NameMap {
  private final Map<QualifiedName, AstType> types = new HashMap<>();
  private final Map<QualifiedName, AstEntity> entities = new HashMap<>();

  public NameMap() {}

  public void addType(AstType type) {
    types.put(type.getName(), type);
  }

  public AstType getType(QualifiedName name) {
    return types.get(name);
  }

  public void addEntity(AstFunction function) {
    entities.put(function.getHeader().getName(), function);
  }

  public void addEntity(AstVariable variable) {
    entities.put(variable.getName(), variable);
  }

  public AstEntity getEntity(QualifiedName name) {
    return entities.get(name);
  }
}
