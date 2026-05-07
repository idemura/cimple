package com.github.idemura.cimple.compiler.ast;

public abstract sealed class AstStatement extends AstNode
    permits AstDefer, AstExpressionStatement, AstFor, AstGoto, AstIf, AstLocal, AstReturn {
  protected AstStatement() {
    super();
  }
}
