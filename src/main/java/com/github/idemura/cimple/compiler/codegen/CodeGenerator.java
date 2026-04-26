package com.github.idemura.cimple.compiler.codegen;

import com.github.idemura.cimple.compiler.ast.AstNode;

public abstract class CodeGenerator {
  public abstract void generateCode(AstNode root);
}
