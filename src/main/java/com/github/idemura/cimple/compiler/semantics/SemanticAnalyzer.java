package com.github.idemura.cimple.compiler.semantics;

import com.github.idemura.cimple.compiler.ErrorConsumer;
import com.github.idemura.cimple.compiler.ast.AstModule;
import com.github.idemura.cimple.compiler.parser.Keyword;

public class SemanticAnalyzer {
  private final ErrorConsumer errorConsumer;

  public SemanticAnalyzer(ErrorConsumer errorConsumer) {
    this.errorConsumer = errorConsumer;
  }

  public boolean analyze(AstModule module) {
    var nameMap = new NameMap();
    module.accept(new PreprocessVisitor(nameMap, Keyword.valueList(), errorConsumer));
    if (errorConsumer.errorCount() > 0) {
      return false;
    }
    // First resolve leaf types. Because visitor traversal does not guarantee the order we
    // need here, doing this in a single phase could expose entities whose types are still
    // unresolved.
    module.accept(new TypeResolutionVisitor(nameMap, errorConsumer));
    if (errorConsumer.errorCount() > 0) {
      return false;
    }
    module.accept(new NameResolutionVisitor(nameMap, errorConsumer));
    if (errorConsumer.errorCount() > 0) {
      return false;
    }
    return true;
  }
}
