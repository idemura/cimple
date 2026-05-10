package com.github.idemura.cimple.compiler.semantics;

import static com.google.common.base.Preconditions.checkArgument;

import com.github.idemura.cimple.compiler.Identifier;
import com.github.idemura.cimple.compiler.ast.AstBuiltinType;
import com.github.idemura.cimple.compiler.ast.AstEntity;
import com.github.idemura.cimple.compiler.ast.AstFunction;
import com.github.idemura.cimple.compiler.ast.AstType;
import com.github.idemura.cimple.compiler.ast.AstVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NameMap {
  private final Map<Identifier, AstType> typeQualifiedNameMap = new HashMap<>();
  private final Map<String, AstType> typeNameMap = new HashMap<>();
  private final Map<Identifier, AstEntity> entityQualifiedNameMap = new HashMap<>();
  private final Map<String, AstEntity> entityNameMap = new HashMap<>();
  private final Map<Identifier, AstFunction> receiverFunctionMap = new HashMap<>();
  private final List<AstEntity> shadowed = new ArrayList<>();
  private final List<String> localNames = new ArrayList<>();

  public NameMap() {}

  public AstType addType(AstType type) {
    var name = type.name();
    var existing = typeQualifiedNameMap.putIfAbsent(name, type);
    if (existing != null) {
      return existing;
    }
    typeNameMap.put(name.typeName(), type);
    return null;
  }

  public AstEntity addFunction(AstFunction function) {
    var name = function.name();
    if (name.typeName() != null) {
      return receiverFunctionMap.putIfAbsent(name, function);
    }
    return addEntity(function);
  }

  public AstEntity addVariable(AstVariable variable) {
    return addEntity(variable);
  }

  private AstEntity addEntity(AstEntity entity) {
    var existing = entityQualifiedNameMap.putIfAbsent(entity.name(), entity);
    if (existing != null) {
      return existing;
    }
    entityNameMap.putIfAbsent(entity.name().entityName(), entity);
    return null;
  }

  public AstEntity addLocal(AstVariable variable) {
    checkArgument(variable.isAnyOf(AstVariable.PARAMETER | AstVariable.LOCAL));
    var name = variable.name().entityName();
    var existing = entityNameMap.get(name);
    if (existing == null) {
      entityNameMap.put(name, variable);
      localNames.add(name);
      return null;
    }
    if (existing instanceof AstVariable existingVariable
        && existingVariable.isAnyOf(AstVariable.PARAMETER | AstVariable.LOCAL)) {
      return existing;
    }
    shadowed.add(existing);
    entityNameMap.put(name, variable);
    localNames.add(name);
    return null;
  }

  public void beginScope() {}

  public void endScope() {
    for (var name : localNames) {
      entityNameMap.remove(name);
    }
    localNames.clear();
    for (var entity : shadowed) {
      entityNameMap.put(entity.name().entityName(), entity);
    }
    shadowed.clear();
  }

  public AstType lookupType(Identifier name) {
    var builtinType = AstBuiltinType.lookup(name.typeName());
    if (builtinType != null) {
      return builtinType;
    }
    if (name.moduleName() != null) {
      return typeQualifiedNameMap.get(name);
    }
    return typeNameMap.get(name.typeName());
  }

  public AstEntity lookupEntity(Identifier name) {
    if (name.moduleName() != null) {
      return entityQualifiedNameMap.get(name);
    }
    return entityNameMap.get(name.entityName());
  }

  public AstFunction lookupReceiverFunction(Identifier name) {
    return receiverFunctionMap.get(name);
  }
}
