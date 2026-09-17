package io.lang.cimple.compiler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GlobalNameMap {
  private record FunctionKey(String name, int parameterCount) {
    private static FunctionKey of(FunctionSignature signature) {
      return new FunctionKey(signature.name(), signature.parameterTypes().size());
    }
  }

  private final Map<Identifier, AstType> typeMap = new LinkedHashMap<>();
  private final Map<Identifier, AstVariable> variableMap = new LinkedHashMap<>();
  private final Map<FunctionKey, List<AstFunction>> functionMap = new LinkedHashMap<>();

  public GlobalNameMap() {}

  public AstType addType(AstType type) {
    return typeMap.putIfAbsent(type.name(), type);
  }

  public AstFunction addFunction(AstFunction function) {
    var signature = function.signature();
    var functions =
        functionMap.computeIfAbsent(FunctionKey.of(signature), ignored -> new ArrayList<>());
    for (var existing : functions) {
      if (existing.signature().equals(signature)) {
        return existing;
      }
    }
    functions.add(function);
    return null;
  }

  public AstVariable addVariable(AstVariable variable) {
    return variableMap.putIfAbsent(variable.name(), variable);
  }

  public Map<String, AstType> collectTypes(String moduleName, ErrorConsumer errorConsumer) {
    var result = new LinkedHashMap<String, AstType>();
    for (var type : typeMap.values()) {
      var name = type.name();
      if (moduleName.equals(name.module())) {
        var existing = result.putIfAbsent(name.entity(), type);
        if (existing != null) {
          errorConsumer.errorAt(
              type.location(),
              "Duplicate type: '%s'. Defined at %s.",
              type.name(),
              existing.location());
        }
      }
    }
    return result;
  }

  public LocalNameMap collectVariables(String moduleName, ErrorConsumer errorConsumer) {
    var result = new LocalNameMap();
    for (var variable : variableMap.values()) {
      var name = variable.name();
      if (moduleName.equals(name.module())) {
        var existing = result.addVariable(variable);
        if (existing != null) {
          errorEntityCollision(errorConsumer, variable, existing);
        }
      }
    }
    return result;
  }

  public AstFunction lookupFunction(FunctionSignature signature) {
    var functions = functionMap.get(FunctionKey.of(signature));
    if (functions == null) {
      return null;
    }
    AstFunction result = null;
    for (var function : functions) {
      if (function.signature().equals(signature)) {
        if (result != null) {
          return null;
        }
        result = function;
      }
    }
    return result;
  }

  public FunctionMatch lookupFunctionMatch(FunctionSignature signature) {
    var function = lookupFunction(signature);
    if (function == null) {
      return null;
    }
    var substitutions = function.signature().match(signature);
    return substitutions == null ? null : new FunctionMatch(function, substitutions);
  }

  public AstVariable lookupVariable(Identifier name) {
    return variableMap.get(name);
  }

  public AstType lookupType(Identifier name) {
    if (name.isBuiltin()) {
      return lookupBuiltinType(name.entity());
    }
    return typeMap.get(name);
  }

  private static String entityKind(AstEntity entity) {
    return switch (entity) {
      case AstFunction ignored -> "function";
      case AstVariable ignored -> "variable";
    };
  }

  private static void errorEntityCollision(
      ErrorConsumer errorConsumer, AstEntity entity, AstEntity existing) {
    errorConsumer.errorAt(
        entity.location(),
        "Definition of %s '%s' has a name collision with %s defined at %s",
        entityKind(entity),
        entity.name().entity(),
        entityKind(existing),
        existing.location());
  }

  static AstType lookupBuiltinType(String name) {
    if ("string".equals(name)) {
      return AstStringType.INSTANCE;
    }
    return AstBuiltinType.lookup(name);
  }
}
