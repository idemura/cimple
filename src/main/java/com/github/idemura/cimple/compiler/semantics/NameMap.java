package com.github.idemura.cimple.compiler.semantics;

import com.github.idemura.cimple.compiler.ast.AstFunction;
import com.github.idemura.cimple.compiler.ast.AstType;
import com.github.idemura.cimple.compiler.ast.AstVariable;
import com.github.idemura.cimple.compiler.ast.QualifiedName;
import java.util.HashMap;
import java.util.Map;

public class NameMap {
  private final Map<QualifiedName, AstType> types = new HashMap<>();
  private final Map<QualifiedName, AstVariable> variables = new HashMap<>();
  private final Map<QualifiedName, AstFunction> functions = new HashMap<>();

  public NameMap() {}

  public void addType(AstType type) {
    types.put(type.getName(), type);
  }

  public void addFunction(AstFunction function) {
    functions.put(function.getHeader().getName(),  function);
  }

  public void addVariable(AstVariable variable) {
    variables.put(variable.getName(), variable);
  }
}
