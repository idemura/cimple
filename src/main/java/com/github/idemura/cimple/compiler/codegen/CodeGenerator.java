package com.github.idemura.cimple.compiler.codegen;

import com.github.idemura.cimple.compiler.ast.AstModule;

public abstract class CodeGenerator {
  public abstract void generateCode(AstModule module);
}
