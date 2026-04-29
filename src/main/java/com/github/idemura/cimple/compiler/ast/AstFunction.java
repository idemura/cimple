package com.github.idemura.cimple.compiler.ast;

public final class AstFunction extends AstNode {
  private AstFunctionHeader header = new AstFunctionHeader();
  private AstBlock block;

  @Override
  public int hashCode() {
    return header.getName().hashCode();
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    return (object instanceof AstFunction other)
        && header.getName().equals(other.getHeader().getName());
  }

  @Override
  public String toString() {
    return "FUNCTION %s(%s): %s"
        .formatted(header.getName(), header.getParameters(), header.getResultType());
  }

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  public AstFunctionHeader getHeader() {
    return header;
  }

  public void setHeader(AstFunctionHeader header) {
    this.header = header;
  }

  public AstBlock getBlock() {
    return block;
  }

  public void setBlock(AstBlock block) {
    this.block = block;
  }
}
