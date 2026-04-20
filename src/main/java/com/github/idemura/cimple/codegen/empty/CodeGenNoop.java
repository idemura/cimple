package com.github.idemura.cimple.codegen.empty;

import com.github.idemura.cimple.compiler.CodeGen;
import com.github.idemura.cimple.compiler.VisitorNode;

public class CodeGenNoop implements CodeGen {
  @Override
  public void generateCode(VisitorNode root) {}
}
