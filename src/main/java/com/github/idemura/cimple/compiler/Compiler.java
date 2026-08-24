package com.github.idemura.cimple.compiler;

import com.github.idemura.cimple.compiler.ast.AstModule;
import com.github.idemura.cimple.compiler.ast.PrintAstVisitor;
import com.github.idemura.cimple.compiler.codegen.CodeGenerator;
import com.github.idemura.cimple.compiler.parser.Parser;
import com.github.idemura.cimple.compiler.parser.Tokenizer;
import com.github.idemura.cimple.compiler.semantics.SemanticAnalyzer;
import java.util.ArrayList;
import java.util.List;

public class Compiler {
  private final CompilerParams params;
  private final IndentWriter debugOutput;
  private final ErrorConsumer errorConsumer;
  private final CodeGenerator codeGenerator;

  public Compiler(CompilerParams params, ErrorConsumer errorConsumer, CodeGenerator codeGenerator) {
    this.params = params;
    this.debugOutput = new IndentWriter(params.debugOutput(), params.indent());
    this.errorConsumer = errorConsumer;
    this.codeGenerator = codeGenerator;
  }

  public boolean compile(List<String> codeList) {
    List<AstModule> modules;
    try {
      modules = parseModules(codeList);
    } catch (CompilerException e) {
      // Fatal frontend errors are reported through the error consumer.
      return false;
    }
    // Keep the control flow explicit in case earlier phases start reporting recoverable errors.
    if (errorConsumer.errorCount() > 0) {
      return false;
    }
    var analyzer = new SemanticAnalyzer(errorConsumer);
    if (!analyzer.analyze(modules)) {
      return false;
    }
    if (params.printAst()) {
      debugOutput.writeLine("Analyzed\n");
      for (var module : modules) {
        new PrintAstVisitor(debugOutput).print(module);
      }
    }
    // Code generation is not expected to report user-facing diagnostics.
    codeGenerator.generateCode(modules);
    return true;
  }

  private List<AstModule> parseModules(List<String> codeList) {
    var modules = new ArrayList<AstModule>();
    for (var code : codeList) {
      modules.add(parseModule(code));
    }
    return modules;
  }

  private AstModule parseModule(String code) {
    var tokenizer = new Tokenizer(errorConsumer);
    tokenizer.split(code, null);
    if (params.printTokens()) {
      debugOutput.writeLine(tokenizer.tokenList().toString());
      debugOutput.writeLine("\n");
    }
    var module = new Parser(tokenizer, errorConsumer).parse();
    if (params.printAst()) {
      debugOutput.writeLine("Parse tree\n");
      new PrintAstVisitor(debugOutput).print(module);
    }
    return module;
  }
}
