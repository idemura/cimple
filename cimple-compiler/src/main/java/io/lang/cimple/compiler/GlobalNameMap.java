package io.lang.cimple.compiler;

import io.lang.cimple.compiler.ast.AstBuiltinType;
import io.lang.cimple.compiler.ast.AstEntity;
import io.lang.cimple.compiler.ast.AstFunction;
import io.lang.cimple.compiler.ast.AstModule;
import io.lang.cimple.compiler.ast.AstStringType;
import io.lang.cimple.compiler.ast.AstType;
import io.lang.cimple.compiler.ast.AstVariable;
import java.util.LinkedHashMap;
import java.util.Map;

public class GlobalNameMap {
  private final Map<Identifier, AstType> typeMap = new LinkedHashMap<>();
  private final Map<Identifier, AstEntity> entityMap = new LinkedHashMap<>();

  public GlobalNameMap() {}

  public AstType addType(AstType type) {
    return typeMap.putIfAbsent(type.name(), type);
  }

  public AstEntity addFunction(AstFunction function) {
    return addEntity(function);
  }

  public AstEntity addVariable(AstVariable variable) {
    return addEntity(variable);
  }

  public Map<String, AstType> collectTypes(AstModule module, ErrorConsumer errorConsumer) {
    var result = new LinkedHashMap<String, AstType>();
    for (var type : typeMap.values()) {
      var name = type.name();
      if (module.name().equals(name.module())) {
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

  public LocalNameMap collectFunctionsAndVariables(AstModule module, ErrorConsumer errorConsumer) {
    var result = new LocalNameMap();
    for (var entity : entityMap.values()) {
      var name = entity.name();
      if (module.name().equals(name.module())) {
        var existing = result.addEntity(entity);
        if (existing != null) {
          errorEntityCollision(errorConsumer, entity, existing);
        }
      }
    }
    return result;
  }

  public AstType lookupType(Identifier name) {
    if (name.isBuiltin()) {
      return lookupBuiltinType(name.type());
    }
    return typeMap.get(name);
  }

  public AstEntity lookupEntity(Identifier name) {
    return entityMap.get(name);
  }

  private AstEntity addEntity(AstEntity entity) {
    return entityMap.putIfAbsent(entity.name(), entity);
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
