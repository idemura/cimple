package com.github.idemura.cimple.compiler.ast;

public abstract sealed class AstStatement extends AstNode
    permits AstBreak,
        AstDefer,
        AstDelete,
        AstExpressionStatement,
        AstFor,
        AstIf,
        AstLocal,
        AstReturn {
  protected AstStatement() {
    super();
  }
}
