package com.github.idemura.cimple.compiler.semantics;

import com.github.idemura.cimple.compiler.ast.AstModule;

public class SemanticAnalyzer {
  public SemanticAnalyzer() {}

  public void analyze(AstModule module) {
    module.accept(new PreprocessRewriteVisitor());
    module.accept(new TypeChecker());
  }
}
