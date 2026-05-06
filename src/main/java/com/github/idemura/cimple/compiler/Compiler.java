package com.github.idemura.cimple.compiler;

import com.github.idemura.cimple.compiler.ast.PrintAstVisitor;
import com.github.idemura.cimple.compiler.codegen.CodeGenerator;
import com.github.idemura.cimple.compiler.parser.Parser;
import com.github.idemura.cimple.compiler.parser.Tokenizer;
import com.github.idemura.cimple.compiler.semantics.SemanticAnalyzer;

public class Compiler {
  private final CompilerParams params;
  private final IndentWriter debugOutput;
  private final ErrorConsumer errorConsumer;
  private final CodeGenerator codeGenerator;

  public Compiler(
      CompilerParams params,
      Appendable debugOutput,
      ErrorConsumer errorConsumer,
      CodeGenerator codeGenerator) {
    this.params = params;
    this.debugOutput = new IndentWriter(debugOutput, 2);
    this.errorConsumer = errorConsumer;
    this.codeGenerator = codeGenerator;
  }

  public void compile(String fileName, String code) {
    var tokenizer = new Tokenizer(errorConsumer);
    tokenizer.split(code, fileName);
    if (params.printTokens()) {
      debugOutput.writeLine(tokenizer.tokenList().toString());
      debugOutput.writeLine("\n");
    }
    var module = new Parser(tokenizer, errorConsumer).parse();
    if (params.printAst()) {
      debugOutput.writeLine("Parse tree\n");
      new PrintAstVisitor(debugOutput).print(module);
    }
    var analyzer = new SemanticAnalyzer(errorConsumer);
    analyzer.analyze(module);
    if (params.printAst()) {
      debugOutput.writeLine("Analyzed\n");
      new PrintAstVisitor(debugOutput).print(module);
    }
    // Outside of try because codegen should not generate user errors.
    codeGenerator.generateCode(module);
  }
}
