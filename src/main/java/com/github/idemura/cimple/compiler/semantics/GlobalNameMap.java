package com.github.idemura.cimple.compiler.semantics;

import com.github.idemura.cimple.compiler.Identifier;
import com.github.idemura.cimple.compiler.ast.AstBuiltinType;
import com.github.idemura.cimple.compiler.ast.AstEntity;
import com.github.idemura.cimple.compiler.ast.AstFunction;
import com.github.idemura.cimple.compiler.ast.AstStringType;
import com.github.idemura.cimple.compiler.ast.AstType;
import com.github.idemura.cimple.compiler.ast.AstVariable;
import java.util.HashMap;
import java.util.Map;

public class GlobalNameMap {
  private final Map<Identifier, AstType> typeQualifiedNameMap = new HashMap<>();
  private final Map<Identifier, AstEntity> entityQualifiedNameMap = new HashMap<>();
  private final Map<Identifier, AstFunction> methodMap = new HashMap<>();

  public GlobalNameMap() {}

  public AstType addType(AstType type) {
    return typeQualifiedNameMap.putIfAbsent(type.name(), type);
  }

  public AstEntity addFunction(AstFunction function) {
    var name = function.name();
    if (name.typeName() != null) {
      return methodMap.putIfAbsent(name, function);
    }
    return addEntity(function);
  }

  public AstEntity addVariable(AstVariable variable) {
    return addEntity(variable);
  }

  public LocalNameMap populateModuleShortNames(String moduleName) {
    var result = new LocalNameMap();
    for (var entry : typeQualifiedNameMap.entrySet()) {
      var name = entry.getKey();
      if (moduleName.equals(name.moduleName())) {
        result.addType(entry.getValue());
      }
    }
    for (var entry : entityQualifiedNameMap.entrySet()) {
      var name = entry.getKey();
      if (moduleName.equals(name.moduleName())) {
        result.addEntity(entry.getValue());
      }
    }
    return result;
  }

  public AstType lookupType(Identifier name) {
    if (name.moduleName() == null) {
      return null;
    }
    if (name.isBuiltin()) {
      return lookupBuiltinType(name.typeName());
    }
    return typeQualifiedNameMap.get(name);
  }

  public AstEntity lookupEntity(Identifier name) {
    if (name.moduleName() == null) {
      return null;
    }
    return entityQualifiedNameMap.get(name);
  }

  public AstFunction lookupMethod(Identifier name) {
    return methodMap.get(name);
  }

  private AstEntity addEntity(AstEntity entity) {
    return entityQualifiedNameMap.putIfAbsent(entity.name(), entity);
  }

  static AstType lookupBuiltinType(String name) {
    if ("string".equals(name)) {
      return AstStringType.INSTANCE;
    }
    return AstBuiltinType.lookup(name);
  }
}
