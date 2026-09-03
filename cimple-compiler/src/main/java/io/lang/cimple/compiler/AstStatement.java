package io.lang.cimple.compiler;

public abstract sealed class AstStatement extends AstNode
    permits AstBreak,
        AstDefer,
        AstDelete,
        AstExpressionStatement,
        AstFor,
        AstIf,
        AstLocal,
        AstReturn {
  protected AstStatement(Location location) {
    super(location);
  }
}
