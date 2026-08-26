package com.github.idemura.cimple.compiler.semantics;

import static com.google.common.base.Preconditions.checkArgument;

import com.github.idemura.cimple.compiler.ErrorConsumer;
import com.github.idemura.cimple.compiler.Identifier;
import com.github.idemura.cimple.compiler.ast.AstArrayType;
import com.github.idemura.cimple.compiler.ast.AstBuiltinType;
import com.github.idemura.cimple.compiler.ast.AstEntity;
import com.github.idemura.cimple.compiler.ast.AstFunction;
import com.github.idemura.cimple.compiler.ast.AstPointerType;
import com.github.idemura.cimple.compiler.ast.AstStringType;
import com.github.idemura.cimple.compiler.ast.AstType;
import com.github.idemura.cimple.compiler.ast.AstAliasType;
import com.github.idemura.cimple.compiler.ast.AstTypeRef;
import com.github.idemura.cimple.compiler.ast.AstVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NameMap {
  private final Map<Identifier, AstType> typeQualifiedNameMap = new HashMap<>();
  private final Map<String, AstType> typeNameMap = new HashMap<>();
  private final Map<Identifier, AstEntity> entityQualifiedNameMap = new HashMap<>();
  private final Map<String, AstEntity> entityNameMap = new HashMap<>();
  private final Map<Identifier, AstFunction> methodMap = new HashMap<>();
  private final List<Scope> scopes = new ArrayList<>();

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
      return methodMap.putIfAbsent(name, function);
    }
    return addEntity(function);
  }

  public AstEntity addVariable(AstVariable variable) {
    return addEntity(variable);
  }

  public void resolveAliases(ErrorConsumer errorConsumer) {
    var states = new IdentityHashMap<AstAliasType, AliasState>();
    for (var type : List.copyOf(typeQualifiedNameMap.values())) {
      if (type instanceof AstAliasType alias) {
        resolveAlias(alias, states, errorConsumer);
      }
    }
    collapseAliasMap(typeQualifiedNameMap, states, errorConsumer);
    collapseAliasMap(typeNameMap, states, errorConsumer);
    rekeyMethods(states, errorConsumer);
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
      currentScope().localNames.add(name);
      return null;
    }
    // Java-style rule: a local or parameter in any active scope blocks redeclaration in nested
    // scopes. Module-level variables and functions may still be shadowed by locals.
    if (isLocalOrParameter(existing)) {
      return existing;
    }
    currentScope().shadowed.put(name, existing);
    entityNameMap.put(name, variable);
    currentScope().localNames.add(name);
    return null;
  }

  public void beginScope() {
    scopes.add(new Scope());
  }

  public void endScope() {
    var scope = scopes.remove(scopes.size() - 1);
    for (var name : scope.localNames) {
      entityNameMap.remove(name);
    }
    entityNameMap.putAll(scope.shadowed);
  }

  public AstType lookupType(Identifier name) {
    if ("string".equals(name.typeName())) {
      return AstStringType.INSTANCE;
    }
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

  public AstFunction lookupMethod(Identifier name) {
    return methodMap.get(name);
  }

  private <T> void collapseAliasMap(
      Map<T, AstType> map,
      IdentityHashMap<AstAliasType, AliasState> states,
      ErrorConsumer errorConsumer) {
    for (var entry : map.entrySet()) {
      entry.setValue(resolveAliasTarget(entry.getValue(), states, errorConsumer));
    }
  }

  private void rekeyMethods(
      IdentityHashMap<AstAliasType, AliasState> states, ErrorConsumer errorConsumer) {
    var resolvedMap = new HashMap<Identifier, AstFunction>();
    for (var function : List.copyOf(methodMap.values())) {
      var objectType = resolveAliasTarget(function.header().objectType(), states, errorConsumer);
      function.header().objectType(objectType);
      if (function.header().objectIndex() >= 0) {
        function.header().parameters().get(function.header().objectIndex()).type(objectType);
      }
      var methodName =
          new Identifier(
              function.name().moduleName(),
              objectType.name().typeName(),
              function.name().entityName());
      function.name(methodName);
      var existing = resolvedMap.putIfAbsent(methodName, function);
      if (existing != null) {
        errorConsumer.errorAt(
            function.location(),
            "Method '%s' has a name collision with method defined at %s",
            methodName,
            existing.location());
      }
    }
    methodMap.clear();
    methodMap.putAll(resolvedMap);
  }

  private AstType resolveAliasTarget(
      AstType type, IdentityHashMap<AstAliasType, AliasState> states, ErrorConsumer errorConsumer) {
    if (type instanceof AstTypeRef typeRef) {
      var resolvedType = lookupType(typeRef.name());
      if (resolvedType == null) {
        errorConsumer.errorAt(type.location(), "Undefined type: '%s'", type.name());
        return AstBuiltinType.VOID;
      }
      return resolveAliasTarget(resolvedType, states, errorConsumer);
    }
    if (type instanceof AstAliasType alias) {
      return resolveAlias(alias, states, errorConsumer);
    }
    if (type instanceof AstPointerType pointerType) {
      pointerType.baseType(resolveAliasTarget(pointerType.baseType(), states, errorConsumer));
      return pointerType;
    }
    if (type instanceof AstArrayType arrayType) {
      arrayType.baseType(resolveAliasTarget(arrayType.baseType(), states, errorConsumer));
      return arrayType;
    }
    return type;
  }

  private AstType resolveAlias(
      AstAliasType alias,
      IdentityHashMap<AstAliasType, AliasState> states,
      ErrorConsumer errorConsumer) {
    var state = states.get(alias);
    if (state == AliasState.RESOLVED) {
      return alias.targetType();
    }
    if (state == AliasState.RESOLVING) {
      errorConsumer.errorAt(alias.location(), "Circular type alias: '%s'", alias.name());
      alias.targetType(AstBuiltinType.VOID);
      states.put(alias, AliasState.RESOLVED);
      return AstBuiltinType.VOID;
    }

    states.put(alias, AliasState.RESOLVING);
    alias.targetType(resolveAliasTarget(alias.targetType(), states, errorConsumer));
    states.put(alias, AliasState.RESOLVED);
    return alias.targetType();
  }

  private Scope currentScope() {
    if (scopes.isEmpty()) {
      beginScope();
    }
    return scopes.getLast();
  }

  private static boolean isLocalOrParameter(AstEntity entity) {
    return entity instanceof AstVariable variable
        && variable.isAnyOf(AstVariable.PARAMETER | AstVariable.LOCAL);
  }

  // Scope frames track temporary unqualified bindings introduced by parameters and locals.
  // Qualified module-level maps are stable and do not participate in local scope cleanup.
  private static final class Scope {
    private final List<String> localNames = new ArrayList<>();
    private final Map<String, AstEntity> shadowed = new LinkedHashMap<>();
  }

  private enum AliasState {
    RESOLVING,
    RESOLVED
  }
}
