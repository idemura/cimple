package com.github.idemura.cimple.compiler.semantics;

import static com.google.common.base.Preconditions.checkNotNull;

import com.github.idemura.cimple.compiler.ast.QualifiedName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ScopeNameMap<T> {
  private final Map<QualifiedName, T> globalNameMap = new HashMap<>();
  private final Map<QualifiedName, T> nameMap = new HashMap<>();
  private final List<List<QualifiedName>> scopeStack = new ArrayList<>();

  void pushScope() {
    scopeStack.add(new ArrayList<>());
  }

  void popScope() {
    for (var name : scopeStack.removeLast()) {
      var globalDef = globalNameMap.get(name);
      if (globalDef == null) {
        nameMap.remove(name);
      } else {
        nameMap.put(name, globalDef);
      }
    }
  }

  // Returns conflicting entity if found.
  T putGlobal(QualifiedName name, T entity) {
    checkNotNull(name);

    var e = globalNameMap.get(name);
    if (e != null) {
      // Error - same name in the scope.
      return e;
    }

    globalNameMap.put(name, entity);
    nameMap.put(name, entity);

    return null;
  }

  // Returns conflicting entity if found.
  T put(QualifiedName name, T entity) {
    checkNotNull(name);

    var e = nameMap.get(name);
    if (e != null) {
      // We allow override module level entities
      if (!globalNameMap.containsKey(name)) {
        return e;
      }
    }
    nameMap.put(name, entity);
    scopeStack.getLast().add(name);
    return null;
  }

  T get(QualifiedName name) {
    return nameMap.get(name);
  }
}
