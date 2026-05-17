package com.github.idemura.cimple.compiler.codegen;

import com.github.idemura.cimple.compiler.ast.AstModule;

public class NoopCodeGenerator extends CodeGenerator {
  @Override
  public void generateCode(AstModule module) {}
}
