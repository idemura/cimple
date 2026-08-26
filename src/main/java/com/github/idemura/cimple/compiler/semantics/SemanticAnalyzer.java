package com.github.idemura.cimple.compiler.semantics;

import com.github.idemura.cimple.compiler.ErrorConsumer;
import com.github.idemura.cimple.compiler.ast.AstEntity;
import com.github.idemura.cimple.compiler.ast.AstFunction;
import com.github.idemura.cimple.compiler.ast.AstModule;
import com.github.idemura.cimple.compiler.ast.AstType;
import com.github.idemura.cimple.compiler.ast.AstVariable;
import com.github.idemura.cimple.compiler.parser.Keyword;
import java.util.List;

public class SemanticAnalyzer {
  private final ErrorConsumer errorConsumer;
  private final GlobalNameMap globalNameMap = new GlobalNameMap();
  private final ReservedWords reservedWords =
      new ReservedWords(Keyword.reservedNames(), Keyword.reservedTypeNames());

  public SemanticAnalyzer(ErrorConsumer errorConsumer) {
    this.errorConsumer = errorConsumer;
  }

  public boolean analyze(List<AstModule> modules) {
    return analyzeWithContext(modules.stream().map(AnalyzerContext::new).toList());
  }

  private static class AnalyzerContext {
    AnalyzerContext(AstModule module) {
      this.module = module;
    }

    AstModule module;
    LocalNameMap localNameMap;
  }

  private boolean analyzeWithContext(List<AnalyzerContext> contexts) {
    for (var context : contexts) {
      var module = context.module;
      module.accept(new PreprocessVisitor(reservedWords, errorConsumer));
    }
    if (hasErrors()) {
      return false;
    }
    populateNameMap(contexts);
    if (hasErrors()) {
      return false;
    }
    for (var context : contexts) {
      var module = context.module;
      context.localNameMap = globalNameMap.populateModuleShortNames(module.name());
      // TODO: Include import names.
      module.accept(
          new TypeRefResolutionVisitor(globalNameMap, context.localNameMap, errorConsumer));
      new TypeRecursionChecker(errorConsumer).check(module);
      if (hasErrors()) {
        return false;
      }
      // Function values are resolved through AstEntityRef.type(), so every function needs its
      // synthetic function type before any module starts resolving calls.
      for (var def : module.definitions()) {
        if (def instanceof AstFunction function) {
          function.makeLambdaType();
        }
      }
    }
    for (var context : contexts) {
      var module = context.module;
      module.accept(new NameResolutionVisitor(globalNameMap, context.localNameMap, errorConsumer));
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

  private void populateNameMap(List<AnalyzerContext> contexts) {
    // First, collect types. They are used for object resolution.
    for (var context : contexts) {
      var module = context.module;
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
    // Collect functions and variables only after types, so methods can be keyed by object type.
    for (var context : contexts) {
      var module = context.module;
      for (var def : module.definitions()) {
        switch (def) {
          case AstFunction function -> {
            function.name(function.name().withModule(module.name()));
            var existing = globalNameMap.addFunction(function);
            if (existing != null) {
              errorEntityCollision(function, existing);
            }
          }
          case AstVariable variable -> {
            variable.name(variable.name().withModule(module.name()));
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
