package com.github.idemura.cimple.compiler.semantics;

import com.github.idemura.cimple.compiler.QualifiedName;
import com.github.idemura.cimple.compiler.ast.AstBuiltinType;
import com.github.idemura.cimple.compiler.ast.AstEntity;
import com.github.idemura.cimple.compiler.ast.AstFunction;
import com.github.idemura.cimple.compiler.ast.AstType;
import com.github.idemura.cimple.compiler.ast.AstVariable;
import java.util.HashMap;
import java.util.Map;

public class NameMap {
  private final Map<QualifiedName, AstType> typeQualifiedNameMap = new HashMap<>();
  private final Map<String, AstType> typeNameMap = new HashMap<>();
  private final Map<QualifiedName, AstEntity> entityQualifiedNameMap = new HashMap<>();
  private final Map<String, AstEntity> entityNameMap = new HashMap<>();

  public NameMap() {}

  public AstType addType(AstType type) {
    var qname = type.getName();
    var existing = typeQualifiedNameMap.putIfAbsent(qname, type);
    if (existing != null) {
      return existing;
    }
    typeNameMap.put(qname.name(), type);
    return null;
  }

  public AstEntity addFunction(AstFunction function) {
    return entityNameMap.putIfAbsent(function.getHeader().getName().name(), function);
  }

  public AstEntity addVariable(AstVariable variable) {
    return entityNameMap.putIfAbsent(variable.getName().name(), variable);
  }

  public AstType lookupType(QualifiedName name) {
    var builtinType = AstBuiltinType.lookup(name.name());
    if (builtinType != null) {
      return builtinType;
    }
    if (name.moduleName() != null) {
      return typeQualifiedNameMap.get(name);
    }
    return typeNameMap.get(name.name());
  }

  public AstEntity lookupEntity(String name) {
    return entityNameMap.get(name);
  }
}
