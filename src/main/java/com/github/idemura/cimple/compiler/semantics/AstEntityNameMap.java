package com.github.idemura.cimple.compiler.semantics;

import com.github.idemura.cimple.compiler.QualifiedName;
import com.github.idemura.cimple.compiler.ast.AstEntity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class AstEntityNameMap {
  private final Map<String, AstEntity> globalNameMap = new HashMap<>();
  private final Map<String, AstEntity> nameMap = new HashMap<>();
  private final List<List<AstEntity>> scopeStack = new ArrayList<>();

  void pushScope() {
    scopeStack.add(new ArrayList<>());
  }

  void popScope() {
    for (var entity : scopeStack.removeLast()) {
      var globalDef = globalNameMap.get(entity);
      if (globalDef == null) {
        nameMap.remove(entity.getName().name());
      } else {
        nameMap.put(entity.getName().name(), globalDef);
      }
    }
  }

  // Returns conflicting entity if found.
  AstEntity putGlobal(AstEntity entity) {
    var baseName = entity.getName().name();
    var e = globalNameMap.get(baseName);
    if (e != null) {
      // Error - same name in the scope.
      return e;
    }
    globalNameMap.put(baseName, entity);
    nameMap.put(baseName, entity);
    return null;
  }

  // Returns conflicting entity if found.
  AstEntity put(AstEntity entity) {
    var baseName = entity.getName().name();
    var e = nameMap.get(baseName);
    if (e != null) {
      // We allow override module level entities
      if (!globalNameMap.containsKey(baseName)) {
        return e;
      }
    }
    nameMap.put(baseName, entity);
    scopeStack.getLast().add(entity);
    return null;
  }

  AstEntity get(QualifiedName name) {
    return nameMap.get(name);
  }
}
