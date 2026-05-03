package com.github.idemura.cimple.compiler.semantics;

import com.github.idemura.cimple.compiler.ast.AstModule;
import com.github.idemura.cimple.compiler.common.ErrorConsumer;

public class SemanticAnalyzer {
  private final ErrorConsumer errorConsumer;

  public SemanticAnalyzer(ErrorConsumer errorConsumer) {
    this.errorConsumer = errorConsumer;
  }

  public void analyze(AstModule module) {
    module.accept(new PreprocessVisitor(errorConsumer));
    module.accept(new TypeChecker());
  }
}
