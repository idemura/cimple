package io.lang.cimple.compiler.codegen;

import io.lang.cimple.compiler.ast.AstModule;
import java.util.List;

public abstract class CodeGenerator {
  public void generateCode(List<AstModule> modules) {
    for (var module : modules) {
      generateCode(module);
    }
  }

  public abstract void generateCode(AstModule module);
}
