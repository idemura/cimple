package com.github.idemura.cimple.codegen.empty;

import com.github.idemura.cimple.compiler.AstAbstractNode;
import com.github.idemura.cimple.compiler.CodeGen;

public class CodeGenNoop implements CodeGen {
  @Override
  public void generateCode(AstAbstractNode root) {}
}
