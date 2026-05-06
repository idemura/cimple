package com.github.idemura.cimple.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.github.idemura.cimple.compiler.Compiler;
import com.github.idemura.cimple.compiler.CompilerException;
import com.github.idemura.cimple.compiler.CompilerParams;
import com.github.idemura.cimple.compiler.IndentWriter;
import com.github.idemura.cimple.compiler.codegen.NoopCodeGenerator;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CimpleCli implements CompilerParams {
  private static final String DIR_SPLIT = "\n#!SPLIT\n";

  @Parameter() List<String> files = new ArrayList<>();

  @Parameter(names = {"--codegen"})
  String codeGen = "none";

  @Parameter(names = {"--debug"})
  boolean debug;

  @Parameter(names = {"--debug_splits"})
  boolean debugSplits;

  @Parameter(names = {"--debug_print_tokens"})
  boolean debugPrintTokens;

  @Parameter(names = {"--debug_print_ast"})
  boolean debugPrintAst;

  public static PrintStream getErrorOutput() {
    return System.err;
  }

  @Override
  public Appendable getDebugOutput() {
    return System.err;
  }

  @Override
  public boolean printTokens() {
    return debug && debugPrintTokens;
  }

  @Override
  public boolean printAst() {
    return debug && debugPrintAst;
  }

  public static IndentWriter ofPrintStream(PrintStream ps) {
    return new IndentWriter(ps, 2);
  }

  CimpleCli() {}

  void parseCmdLine(String[] args) {
    JCommander.newBuilder().addObject(this).build().parse(args);
  }

  int run() {
    for (var fileName : files) {
      String code;
      try {
        code = Files.readString(Paths.get(fileName));
      } catch (IOException e) {
        printFileError(fileName, e.getMessage());
        return 1;
      }

      try {
        if (debugSplits) {
          int splitCount = 0;
          int first = 0;
          int split = code.indexOf(DIR_SPLIT, first);
          while (split >= 0) {
            compile(fileName, code.substring(first, split));
            first = split + DIR_SPLIT.length();
            split = code.indexOf(DIR_SPLIT, first);
            splitCount++;
          }
          compile(fileName, code.substring(first));
        } else {
          if (!compile(fileName, code)) {
            return 1;
          }
        }
      } catch (CompilerException e) {
        System.err.printf("FATAL ERROR: %s\n", e.getMessage());
        return 1;
      }
    }
    return 0;
  }

  boolean compile(String fileName, String code) {
    return new Compiler(this, System.out, new CliErrorConsumer(), new NoopCodeGenerator())
        .compile(fileName, code);
  }

  private static void printFileError(String fileName, String message) {
    System.err.printf("Error reading file '%s': %s\n", fileName, message);
  }

  public static void main(String[] args) {
    var app = new CimpleCli();
    app.parseCmdLine(args);
    System.exit(app.run());
  }
}
