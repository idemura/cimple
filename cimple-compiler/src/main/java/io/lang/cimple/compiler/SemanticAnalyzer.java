package io.lang.cimple.compiler;

import static io.lang.cimple.compiler.TypeValidator.checkRecursiveTypeDefinitions;

import io.lang.cimple.compiler.ast.AstEntity;
import io.lang.cimple.compiler.ast.AstFunction;
import io.lang.cimple.compiler.ast.AstModule;
import io.lang.cimple.compiler.ast.AstType;
import io.lang.cimple.compiler.ast.AstVariable;
import java.util.List;

public class SemanticAnalyzer {
  private final ErrorConsumer errorConsumer;
  private final GlobalNameMap globalNameMap = new GlobalNameMap();

  public SemanticAnalyzer(ErrorConsumer errorConsumer) {
    this.errorConsumer = errorConsumer;
  }

  public boolean analyze(List<AstModule> modules) {
    for (var module : modules) {
      module.accept(new PreprocessVisitor(errorConsumer));
    }
    if (hasErrors()) {
      return false;
    }
    collectTypes(modules);
    if (hasErrors()) {
      return false;
    }

    for (var module : modules) {
      module.accept(new ResolveTypesVisitor(globalNameMap, errorConsumer));
      checkRecursiveTypeDefinitions(module, errorConsumer);
      if (hasErrors()) {
        return false;
      }
    }

    // After type resolution, variable and free functions remain with nonqualified names.
    for (var module : modules) {
      qualifyTopLevelNames(module);
    }

    collectFunctionsAndVariables(modules);
    if (hasErrors()) {
      return false;
    }

    assignFunctionTypes(modules);
    for (var module : modules) {
      module.accept(new TypeCheckAndResolveNamesVisitor(globalNameMap, errorConsumer));
      if (hasErrors()) {
        return false;
      }
    }

    for (var module : modules) {
      module.accept(new ConstantFoldingVisitor(errorConsumer));
      if (hasErrors()) {
        return false;
      }
    }
    return true;
  }

  GlobalNameMap globalNameMap() {
    return globalNameMap;
  }

  private boolean hasErrors() {
    return errorConsumer.errorCount() > 0;
  }

  private void qualifyTopLevelNames(AstModule module) {
    for (var def : module.definitions()) {
      if (def instanceof AstEntity entity) {
        var name = entity.name();
        if (name.moduleName() == null) {
          entity.name(name.withModule(module.name()));
        }
      }
    }
  }

  private void assignFunctionTypes(List<AstModule> modules) {
    // Function values are resolved through AstEntityRef.type(), so every function needs its
    // synthetic function type before any module starts resolving calls.
    for (var module : modules) {
      for (var def : module.definitions()) {
        if (def instanceof AstFunction function) {
          function.makeLambdaType();
        }
      }
    }
  }

  private void collectTypes(List<AstModule> modules) {
    for (var module : modules) {
      for (var def : module.definitions()) {
        if (def instanceof AstType type) {
          type.name(type.name().withModule(module.name()));
          var existing = globalNameMap.addType(type);
          if (existing != null) {
            errorConsumer.errorAt(
                type.location(),
                "Duplicate type: '%s'. Defined at %s.",
                type.name(),
                existing.location());
          }
        }
      }
    }
  }

  private void collectFunctionsAndVariables(List<AstModule> modules) {
    for (var module : modules) {
      for (var def : module.definitions()) {
        switch (def) {
          case AstFunction function -> {
            var existing = globalNameMap.addFunction(function);
            if (existing != null) {
              errorEntityCollision(function, existing);
            }
          }
          case AstVariable variable -> {
            var existing = globalNameMap.addVariable(variable);
            if (existing != null) {
              errorEntityCollision(variable, existing);
            }
            variable.setBit(AstVariable.GLOBAL);
          }
          default -> {}
        }
      }
    }
  }

  private static String entityKind(AstEntity entity) {
    return switch (entity) {
      case AstFunction ignored -> "function";
      case AstVariable ignored -> "variable";
    };
  }

  private void errorEntityCollision(AstEntity entity, AstEntity existing) {
    errorConsumer.errorAt(
        entity.location(),
        "Definition of %s '%s' has a name collision with %s defined at %s",
        entityKind(entity),
        entity.name().entityName(),
        entityKind(existing),
        existing.location());
  }
}
