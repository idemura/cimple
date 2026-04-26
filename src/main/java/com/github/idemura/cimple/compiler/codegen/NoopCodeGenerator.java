package com.github.idemura.cimple.compiler.codegen;

import com.github.idemura.cimple.compiler.ast.AstNode;

public class NoopCodeGenerator extends CodeGenerator {
  @Override
  public void generateCode(AstNode root) {}
}
