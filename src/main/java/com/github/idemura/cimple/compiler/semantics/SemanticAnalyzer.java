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
  private final NameMap nameMap = new NameMap();

  public SemanticAnalyzer(ErrorConsumer errorConsumer) {
    this.errorConsumer = errorConsumer;
  }

  public boolean analyze(List<AstModule> modules) {
    var reservedWords = new ReservedWords(Keyword.reservedNames(), Keyword.reservedTypeNames());
    for (var module : modules) {
      module.accept(new PreprocessVisitor(reservedWords, errorConsumer));
    }
    if (hasErrors()) {
      return false;
    }
    populateNameMap(modules);
    if (hasErrors()) {
      return false;
    }
    for (var module : modules) {
      module.accept(new TypeRefResolutionVisitor(nameMap, errorConsumer));
    }
    for (var module : modules) {
      new TypeRecursionChecker(errorConsumer).check(module);
    }
    if (hasErrors()) {
      return false;
    }
    assignFunctionTypes(modules);
    for (var module : modules) {
      module.accept(new NameResolutionVisitor(nameMap, errorConsumer));
    }
    if (hasErrors()) {
      return false;
    }
    return true;
  }

  NameMap nameMap() {
    return nameMap;
  }

  private boolean hasErrors() {
    return errorConsumer.errorCount() > 0;
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

  private void populateNameMap(List<AstModule> modules) {
    // First, collect types. They are used for object resolution.
    for (var module : modules) {
      for (var def : module.definitions()) {
        if (def instanceof AstType type) {
          type.name(type.name().withModule(module.name()));
          var existing = nameMap.addType(type);
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
    for (var module : modules) {
      for (var def : module.definitions()) {
        switch (def) {
          case AstFunction function -> {
            function.name(function.name().withModule(module.name()));
            var existing = nameMap.addFunction(function);
            if (existing != null) {
              errorEntityCollision(function, existing);
            }
          }
          case AstVariable variable -> {
            variable.name(variable.name().withModule(module.name()));
            var existing = nameMap.addVariable(variable);
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
