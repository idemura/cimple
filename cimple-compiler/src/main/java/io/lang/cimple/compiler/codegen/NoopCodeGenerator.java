package io.lang.cimple.compiler.codegen;

import io.lang.cimple.compiler.ast.AstModule;

public class NoopCodeGenerator extends CodeGenerator {
  @Override
  public void generateCode(AstModule module) {}
}
