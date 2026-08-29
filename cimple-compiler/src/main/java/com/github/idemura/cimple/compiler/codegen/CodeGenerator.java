package com.github.idemura.cimple.compiler.codegen;

import com.github.idemura.cimple.compiler.ast.AstModule;
import java.util.List;

public abstract class CodeGenerator {
  public void generateCode(List<AstModule> modules) {
    for (var module : modules) {
      generateCode(module);
    }
  }

  public abstract void generateCode(AstModule module);
}
