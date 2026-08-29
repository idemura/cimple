package io.lang.cimple.compiler;

import static com.google.common.base.Preconditions.checkArgument;

import io.lang.cimple.compiler.ast.AstEntity;
import io.lang.cimple.compiler.ast.AstVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LocalNameMap {
  // Scope frames track temporary unqualified bindings introduced by parameters and locals.
  // Module-level short names are restored when a local scope ends.
  private static final class Scope {
    private final List<String> localNames = new ArrayList<>();
    private final Map<String, AstEntity> shadowed = new LinkedHashMap<>();
  }

  private final Map<String, AstEntity> entityNameMap = new HashMap<>();
  private final List<Scope> scopes = new ArrayList<>();

  public LocalNameMap() {}

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

  // TODO: Revisit
  public AstEntity lookupEntity(Identifier name) {
    if (name.moduleName() != null) {
      return null;
    }
    return lookupEntity(name.entityName());
  }

  public AstEntity lookupEntity(String name) {
    return entityNameMap.get(name);
  }

  AstEntity addEntity(AstEntity entity) {
    return entityNameMap.putIfAbsent(entity.name().entityName(), entity);
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
}
