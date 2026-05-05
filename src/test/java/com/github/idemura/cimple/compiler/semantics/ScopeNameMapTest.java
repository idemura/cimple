package com.github.idemura.cimple.compiler.semantics;

import static org.junit.jupiter.api.Assertions.*;

import com.github.idemura.cimple.compiler.QualifiedName;
import com.github.idemura.cimple.compiler.ast.AstVariable;

class ScopeNameMapTest {
  private static AstVariable makeVariable(String name) {
    var v = new AstVariable();
    v.setName(new QualifiedName(name));
    return v;
  }
}
