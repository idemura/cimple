package com.github.idemura.cimple.compiler;

import com.github.idemura.cimple.compiler.ast.AstModule;
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

  public boolean compile(String fileName, String code) {
    AstModule module;
    try {
      var tokenizer = new Tokenizer(errorConsumer);
      tokenizer.split(code, fileName);
      if (params.printTokens()) {
        debugOutput.writeLine(tokenizer.tokenList().toString());
        debugOutput.writeLine("\n");
      }
      module = new Parser(tokenizer, errorConsumer).parse();
      if (params.printAst()) {
        debugOutput.writeLine("Parse tree\n");
        new PrintAstVisitor(debugOutput).print(module);
      }
    } catch (CompilerException e) {
      // Fatal frontend errors are reported through the error consumer.
      return false;
    }
    // Keep the control flow explicit in case earlier phases start reporting recoverable errors.
    if (errorConsumer.errorCount() > 0) {
      return false;
    }
    var analyzer = new SemanticAnalyzer(errorConsumer);
    if (!analyzer.analyze(module)) {
      return false;
    }
    if (params.printAst()) {
      debugOutput.writeLine("Analyzed\n");
      new PrintAstVisitor(debugOutput).print(module);
    }
    // Code generation is not expected to report user-facing diagnostics.
    codeGenerator.generateCode(module);
    return true;
  }
}
