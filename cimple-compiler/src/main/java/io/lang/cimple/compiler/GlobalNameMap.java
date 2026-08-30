package io.lang.cimple.compiler;

import io.lang.cimple.compiler.ast.AstBuiltinType;
import io.lang.cimple.compiler.ast.AstEntity;
import io.lang.cimple.compiler.ast.AstFunction;
import io.lang.cimple.compiler.ast.AstStringType;
import io.lang.cimple.compiler.ast.AstType;
import io.lang.cimple.compiler.ast.AstVariable;
import java.util.LinkedHashMap;
import java.util.Map;

public class GlobalNameMap {
  private final Map<Identifier, AstType> typeMap = new LinkedHashMap<>();
  private final Map<Identifier, AstVariable> variableMap = new LinkedHashMap<>();
  private final Map<FunctionSignature, Map<String, AstFunction>> functionMap =
      new LinkedHashMap<>();

  public GlobalNameMap() {}

  public AstType addType(AstType type) {
    return typeMap.putIfAbsent(type.name(), type);
  }

  public AstFunction addFunction(AstFunction function) {
    return functionMap
        .computeIfAbsent(function.signature(), unused -> new LinkedHashMap<>())
        .putIfAbsent(function.name().module(), function);
  }

  public AstVariable addVariable(AstVariable variable) {
    return variableMap.putIfAbsent(variable.name(), variable);
  }

  public Map<String, AstType> collectTypes(String moduleName, ErrorConsumer errorConsumer) {
    var result = new LinkedHashMap<String, AstType>();
    for (var type : typeMap.values()) {
      var name = type.name();
      if (moduleName.equals(name.module())) {
        var existing = result.putIfAbsent(name.type(), type);
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

  public AstFunction lookupFunction(String moduleName, FunctionSignature signature) {
    var functions = functionMap.get(signature);
    if (functions == null) {
      return null;
    }
    if (moduleName != null) {
      return functions.get(moduleName);
    }
    if (functions.size() != 1) {
      return null;
    }
    return functions.values().iterator().next();
  }

  public AstVariable lookupVariable(Identifier name) {
    return variableMap.get(name);
  }

  public AstType lookupType(Identifier name) {
    if (name.isBuiltin()) {
      return lookupBuiltinType(name.type());
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
