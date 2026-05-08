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
  private record ReceiverFunctionKey(QualifiedName receiverType, String name) {}

  private final Map<QualifiedName, AstType> typeQualifiedNameMap = new HashMap<>();
  private final Map<String, AstType> typeNameMap = new HashMap<>();
  private final Map<QualifiedName, AstEntity> entityQualifiedNameMap = new HashMap<>();
  private final Map<String, AstEntity> entityNameMap = new HashMap<>();
  private final Map<ReceiverFunctionKey, AstFunction> receiverFunctionMap = new HashMap<>();
  private final List<AstEntity> shadowed = new ArrayList<>();
  private final List<String> localNames = new ArrayList<>();

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
    var receiverType = function.getHeader().getReceiverType();
    if (receiverType != null) {
      return receiverFunctionMap.putIfAbsent(
          new ReceiverFunctionKey(receiverType.getName(), function.getName().name()), function);
    }
    return entityNameMap.putIfAbsent(function.getHeader().getName().name(), function);
  }

  public AstEntity addVariable(AstVariable variable) {
    return entityNameMap.putIfAbsent(variable.getName().name(), variable);
  }

  public AstEntity addLocal(AstVariable variable) {
    checkArgument(variable.isAnyOf(AstVariable.PARAMETER | AstVariable.LOCAL));
    var name = variable.getName().name();
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
      entityNameMap.put(entity.getName().name(), entity);
    }
    shadowed.clear();
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

  public AstFunction lookupReceiverFunction(QualifiedName receiverType, String name) {
    return receiverFunctionMap.get(new ReceiverFunctionKey(receiverType, name));
  }
}
