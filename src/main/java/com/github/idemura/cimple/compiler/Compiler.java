package com.github.idemura.cimple.compiler;

import com.github.idemura.cimple.common.IndentWriter;
import com.github.idemura.cimple.compiler.ast.PrintAstVisitor;
import com.github.idemura.cimple.compiler.codegen.CodeGenerator;
import com.github.idemura.cimple.compiler.parser.Parser;
import com.github.idemura.cimple.compiler.tokens.Tokenizer;

public class Compiler {
  private final CompilerParams params;
  private final IndentWriter debugOutput;
  private final CodeGenerator codeGenerator;

  public Compiler(CompilerParams params, IndentWriter debugOutput, CodeGenerator codeGenerator) {
    this.params = params;
    this.debugOutput = debugOutput;
    this.codeGenerator = codeGenerator;
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
    codeGenerator.generateCode(root);
  }
}
