package com.github.idemura.cimple.compiler.semantics;

import com.github.idemura.cimple.compiler.ErrorConsumer;
import com.github.idemura.cimple.compiler.ast.AstTypeRef;
import com.github.idemura.cimple.compiler.ast.AstVisitor;

public class TypeResolutionVisitor extends AstVisitor {
  private final NameMap nameMap;
  private final ErrorConsumer errorConsumer;

  public TypeResolutionVisitor(NameMap nameMap, ErrorConsumer errorConsumer) {
    this.nameMap = nameMap;
    this.errorConsumer = errorConsumer;
  }

  @Override
  public Object visit(AstTypeRef node) {
    var type = nameMap.lookupType(node.name());
    if (type == null) {
      errorConsumer.errorAt(node.location(), "Undefined type: %s", node.name());
    }
    node.type(type);
    return null;
  }
}
