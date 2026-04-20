package com.github.idemura.cimple.compiler;

import com.github.idemura.cimple.common.StringOutput;

public class Compiler {
  private final CompilerParams params;
  private final StringOutput debugOutput;
  private final CodeGen codeGen;

  public Compiler(CompilerParams params, StringOutput debugOutput, CodeGen codeGen) {
    this.params = params;
    this.debugOutput = debugOutput;
    this.codeGen = codeGen;
  }

  public void compile(String fileName, String code) {
    var tokens = new Tokenizer(fileName, code).split();
    if (params.printTokens()) {
      debugOutput.write(tokens.toString());
      debugOutput.write("\n");
    }
    var root = new Parser(params, tokens).parse();
    if (params.printAst()) {
      debugOutput.write("Parse tree\n");
      new PrintVisitor(debugOutput).print(root);
    }
    root.accept(new TypeChecker());
    if (params.printAst()) {
      debugOutput.write("Type checked\n");
      new PrintVisitor(debugOutput).print(root);
    }
    // Outside of try because codegen should not generate user errors.
    codeGen.generateCode(root);
  }
}
