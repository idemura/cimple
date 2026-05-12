package com.github.idemura.cimple.compiler.ast;

public final class AstReceiverLookup extends AstExpression {
  private AstExpression receiver;
  private String functionName;

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {
    receiver.accept(visitor);
  }

  @Override
  public AstExpression rewrite(AstExpressionRewriter rewriter) {
    receiver = receiver.rewrite(rewriter);
    return rewriter.rewrite(this);
  }

  @Override
  public AstType type() {
    return receiver.type();
  }

  public AstExpression receiver() {
    return receiver;
  }

  public void receiver(AstExpression receiver) {
    this.receiver = receiver;
  }

  public String functionName() {
    return functionName;
  }

  public void functionName(String functionName) {
    this.functionName = functionName;
  }
}
