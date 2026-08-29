package com.github.idemura.cimple.compiler.codegen.c;

public record CCodeGeneratorParams(
    CStandard standard, boolean mangleModuleName, boolean outputPreamble) {
  public static final class Builder {
    private CStandard standard = CStandard.def();
    private boolean mangleModuleName = true;
    private boolean outputPreamble = true;

    private Builder() {}

    public Builder standard(CStandard standard) {
      this.standard = standard;
      return this;
    }

    public Builder mangleModuleName(boolean mangleIncludeModuleName) {
      this.mangleModuleName = mangleIncludeModuleName;
      return this;
    }

    public Builder outputPreamble(boolean outputPreamble) {
      this.outputPreamble = outputPreamble;
      return this;
    }

    public CCodeGeneratorParams build() {
      return new CCodeGeneratorParams(standard, mangleModuleName, outputPreamble);
    }
  }

  public static Builder builder() {
    return new Builder();
  }
}
