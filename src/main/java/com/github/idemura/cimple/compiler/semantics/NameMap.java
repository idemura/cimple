package com.github.idemura.cimple.compiler.semantics;

import static com.google.common.base.Preconditions.checkArgument;

import com.github.idemura.cimple.compiler.QualifiedName;
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
  private record ReceiverFunctionKey(QualifiedName receiverType, String name) {
    @Override
    public String toString() {
      return receiverType.toString() + ":" + name;
    }
  }

  private final Map<QualifiedName, AstType> typeQualifiedNameMap = new HashMap<>();
  private final Map<String, AstType> typeNameMap = new HashMap<>();
  private final Map<QualifiedName, AstEntity> entityQualifiedNameMap = new HashMap<>();
  private final Map<String, AstEntity> entityNameMap = new HashMap<>();
  private final Map<ReceiverFunctionKey, AstFunction> receiverFunctionMap = new HashMap<>();
  private final List<AstEntity> shadowed = new ArrayList<>();
  private final List<String> localNames = new ArrayList<>();

  public NameMap() {}

  public AstType addType(AstType type) {
    var name = type.name();
    var existing = typeQualifiedNameMap.putIfAbsent(name, type);
    if (existing != null) {
      return existing;
    }
    typeNameMap.put(name.baseName(), type);
    return null;
  }

  public AstEntity addFunction(AstFunction function) {
    var receiverType = function.header().receiverType();
    if (receiverType != null) {
      return receiverFunctionMap.putIfAbsent(
          new ReceiverFunctionKey(receiverType.name(), function.name().baseName()), function);
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
    entityNameMap.putIfAbsent(entity.name().baseName(), entity);
    return null;
  }

  public AstEntity addLocal(AstVariable variable) {
    checkArgument(variable.isAnyOf(AstVariable.PARAMETER | AstVariable.LOCAL));
    var name = variable.name().baseName();
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
      entityNameMap.put(entity.name().baseName(), entity);
    }
    shadowed.clear();
  }

  public AstType lookupType(QualifiedName name) {
    var builtinType = AstBuiltinType.lookup(name.baseName());
    if (builtinType != null) {
      return builtinType;
    }
    if (name.moduleName() != null) {
      return typeQualifiedNameMap.get(name);
    }
    return typeNameMap.get(name.baseName());
  }

  public AstEntity lookupEntity(QualifiedName name) {
    if (name.moduleName() != null) {
      return entityQualifiedNameMap.get(name);
    }
    return entityNameMap.get(name.baseName());
  }

  public AstFunction lookupReceiverFunction(QualifiedName receiverType, String name) {
    return receiverFunctionMap.get(new ReceiverFunctionKey(receiverType, name));
  }
}
