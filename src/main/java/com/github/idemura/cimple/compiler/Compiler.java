package com.github.idemura.cimple.compiler;

import com.github.idemura.cimple.common.IndentWriter;

public class Compiler {
  private final CompilerParams params;
  private final IndentWriter debugOutput;
  private final CodeGen codeGen;

  public Compiler(CompilerParams params, IndentWriter debugOutput, CodeGen codeGen) {
    this.params = params;
    this.debugOutput = debugOutput;
    this.codeGen = codeGen;
  }

  public void compile(String fileName, String code) {
    var tokens = new Tokenizer(fileName, code).split();
    if (params.printTokens()) {
      debugOutput.writeLine(tokens.toString());
      debugOutput.writeLine("\n");
    }
    var root = new Parser(tokens).parse();
    if (params.printAst()) {
      debugOutput.writeLine("Parse tree\n");
      new PrintAstVisitor(debugOutput).print(root);
    }
    root.accept(new TypeChecker());
    if (params.printAst()) {
      debugOutput.writeLine("Type checked\n");
      new PrintAstVisitor(debugOutput).print(root);
    }
    // Outside of try because codegen should not generate user errors.
    codeGen.generateCode(root);
  }
}
