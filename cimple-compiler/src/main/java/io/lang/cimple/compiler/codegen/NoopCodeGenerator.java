package io.lang.cimple.compiler.codegen;

import io.lang.cimple.compiler.AstModule;

public class NoopCodeGenerator extends CodeGenerator {
  @Override
  public void generateCode(AstModule module) {}
}
