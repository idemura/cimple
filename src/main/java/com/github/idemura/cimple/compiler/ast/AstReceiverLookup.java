package com.github.idemura.cimple.compiler.ast;

public final class AstReceiverLookup extends AstExpression {
  private AstExpression receiver;
  private String functionName;

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  @Override
  public AstExpression acceptRewriter(AstExpressionRewriter rewriter) {
    receiver = receiver.acceptRewriter(rewriter);
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
