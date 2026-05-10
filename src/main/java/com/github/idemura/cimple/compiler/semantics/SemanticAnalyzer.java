package com.github.idemura.cimple.compiler.semantics;

import com.github.idemura.cimple.compiler.ErrorConsumer;
import com.github.idemura.cimple.compiler.QualifiedName;
import com.github.idemura.cimple.compiler.ast.AstEntity;
import com.github.idemura.cimple.compiler.ast.AstFunction;
import com.github.idemura.cimple.compiler.ast.AstModule;
import com.github.idemura.cimple.compiler.ast.AstType;
import com.github.idemura.cimple.compiler.ast.AstVariable;
import com.github.idemura.cimple.compiler.parser.Keyword;

public class SemanticAnalyzer {
  private final ErrorConsumer errorConsumer;
  private final NameMap nameMap = new NameMap();

  public SemanticAnalyzer(ErrorConsumer errorConsumer) {
    this.errorConsumer = errorConsumer;
  }

  public boolean analyze(AstModule module) {
    module.accept(new PreprocessVisitor(Keyword.valueList(), errorConsumer));
    if (hasErrors()) {
      return false;
    }
    populateNameMap(module);
    if (hasErrors()) {
      return false;
    }
    module.accept(new TypeResolutionVisitor(nameMap, errorConsumer));
    module.accept(new NameResolutionVisitor(nameMap, errorConsumer));
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

  private void populateNameMap(AstModule module) {
    // First, collect types. They are used for receiver resolution.
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
    // Collection functions and variables. Resolve receiver type.
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

  private QualifiedName resolveTypeName(QualifiedName typeName, String moduleName) {
    // TODO: Use imports to resolve.
    return typeName.withModule(moduleName);
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
