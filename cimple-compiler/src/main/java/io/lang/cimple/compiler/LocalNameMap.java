package io.lang.cimple.compiler;

import static com.google.common.base.Preconditions.checkArgument;

import io.lang.cimple.compiler.ast.AstVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LocalNameMap {
  // Scope frames track temporary variable bindings introduced by parameters and locals.
  // Module-level variables are restored when a local scope ends.
  private static final class Scope {
    private final List<String> localNames = new ArrayList<>();
    private final Map<String, AstVariable> shadowedVariables = new LinkedHashMap<>();
  }

  private final Map<String, AstVariable> variablesMap = new HashMap<>();
  private final List<Scope> scopes = new ArrayList<>();

  public LocalNameMap() {}

  public AstVariable addLocal(AstVariable variable) {
    checkArgument(variable.isAnyOf(AstVariable.PARAMETER | AstVariable.LOCAL));
    var name = variable.name().entity();
    var existing = variablesMap.get(name);
    if (existing == null) {
      variablesMap.put(name, variable);
      currentScope().localNames.add(name);
      return null;
    }
    // Java-style rule: a local or parameter in any active scope blocks redeclaration in nested
    // scopes. Module-level variables may still be shadowed by locals.
    if (isLocalOrParameter(existing)) {
      return existing;
    }
    currentScope().shadowedVariables.put(name, existing);
    variablesMap.put(name, variable);
    currentScope().localNames.add(name);
    return null;
  }

  public void beginScope() {
    scopes.add(new Scope());
  }

  public void endScope() {
    var scope = scopes.remove(scopes.size() - 1);
    for (var name : scope.localNames) {
      variablesMap.remove(name);
    }
    variablesMap.putAll(scope.shadowedVariables);
  }

  public AstVariable lookupVariable(String name) {
    return variablesMap.get(name);
  }

  AstVariable addVariable(AstVariable variable) {
    return variablesMap.putIfAbsent(variable.name().entity(), variable);
  }

  private Scope currentScope() {
    if (scopes.isEmpty()) {
      beginScope();
    }
    return scopes.getLast();
  }

  private static boolean isLocalOrParameter(AstVariable variable) {
    return variable.isAnyOf(AstVariable.PARAMETER | AstVariable.LOCAL);
  }
}
