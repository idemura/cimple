package com.github.idemura.cimple.compiler.semantics;

import com.github.idemura.cimple.common.ErrorConsumer;
import com.github.idemura.cimple.compiler.ast.AstModule;

public class SemanticAnalyzer {
  private final ErrorConsumer errorConsumer;

  public SemanticAnalyzer(ErrorConsumer errorConsumer) {
    this.errorConsumer = errorConsumer;
  }

  public void analyze(AstModule module) {
    module.accept(new PreprocessRewriteVisitor(errorConsumer));
    module.accept(new TypeChecker());
  }
}
