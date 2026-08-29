package io.lang.cimple.compiler;

import io.lang.cimple.compiler.ast.AstModule;
import io.lang.cimple.compiler.ast.PrintAstVisitor;
import io.lang.cimple.compiler.codegen.CodeGenerator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Compiler {
  private static final Set<String> BUILTIN_FILES = Set.of("lib/_builtin.ci");

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

  public boolean compile(List<SourceCode> sourceCodeList) {
    List<AstModule> modules;
    try {
      modules = parseModules(sourceCodeList);
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

  private List<AstModule> parseModules(List<SourceCode> sourceCodeList) {
    var modules = new ArrayList<AstModule>();
    for (var sourceCode : sourceCodeList) {
      modules.add(parseModule(sourceCode));
    }
    return modules;
  }

  private AstModule parseModule(SourceCode sourceCode) {
    var tokenizer = new Tokenizer(errorConsumer);
    tokenizer.split(sourceCode.code(), sourceCode.fileName());
    if (params.printTokens()) {
      debugOutput.writeLine(tokenizer.tokenList().toString());
      debugOutput.writeLine("\n");
    }
    var module = new Parser(tokenizer, errorConsumer).parse();
    module.builtin(isBuiltinFile(sourceCode.fileName()));
    if (params.printAst()) {
      debugOutput.writeLine("Parse tree\n");
      new PrintAstVisitor(debugOutput).print(module);
    }
    return module;
  }

  private static boolean isBuiltinFile(String fileName) {
    return fileName != null && BUILTIN_FILES.contains(fileName);
  }
}
