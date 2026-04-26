package com.github.idemura.cimple.compiler;

import com.github.idemura.cimple.compiler.ast.AstNode;

public interface CodeGen {
  void generateCode(AstNode root);
}
