package com.github.idemura.cimple.compiler.codegen.c;

public record CCodeGeneratorParams(CStandard standard, boolean mangleModuleName) {
  public static final class Builder {
    private CStandard standard = CStandard.C11;
    private boolean mangleModuleName = true;

    private Builder() {}

    public Builder standard(CStandard standard) {
      this.standard = standard;
      return this;
    }

    public Builder mangleModuleName(boolean mangleIncludeModuleName) {
      this.mangleModuleName = mangleIncludeModuleName;
      return this;
    }

    public CCodeGeneratorParams build() {
      return new CCodeGeneratorParams(standard, mangleModuleName);
    }
  }

  public static Builder builder() {
    return new Builder();
  }
}
