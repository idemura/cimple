package com.github.idemura.cimple.compiler.semantics;

import com.github.idemura.cimple.compiler.ast.AstModule;
import com.github.idemura.cimple.compiler.common.ErrorConsumer;

public class SemanticAnalyzer {
  private final ErrorConsumer errorConsumer;

  public SemanticAnalyzer(ErrorConsumer errorConsumer) {
    this.errorConsumer = errorConsumer;
  }

  public void analyze(AstModule module) {
    var nameMap = new NameMap();
    module.accept(new PreprocessVisitor(nameMap, errorConsumer));
    module.accept(new TypeChecker());
  }
}
