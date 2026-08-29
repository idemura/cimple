package io.lang.cimple.compiler;

import static com.google.common.base.Preconditions.checkNotNull;

public record CompilerParams(
    Appendable debugOutput, boolean printTokens, boolean printAst, int indent) {
  public static final class Builder {
    private Appendable debugOutput = System.err;
    private boolean printTokens;
    private boolean printAst;
    private int indent = 2;

    private Builder() {}

    public Builder debugOutput(Appendable debugOutput) {
      this.debugOutput = debugOutput;
      return this;
    }

    public Builder printTokens(boolean printTokens) {
      this.printTokens = printTokens;
      return this;
    }

    public Builder printAst(boolean printAst) {
      this.printAst = printAst;
      return this;
    }

    public Builder indent(int indent) {
      this.indent = indent;
      return this;
    }

    public CompilerParams build() {
      return new CompilerParams(checkNotNull(debugOutput), printTokens, printAst, indent);
    }
  }

  public static Builder builder() {
    return new Builder();
  }
}
