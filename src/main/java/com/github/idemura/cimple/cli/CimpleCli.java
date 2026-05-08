package com.github.idemura.cimple.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.github.idemura.cimple.compiler.Compiler;
import com.github.idemura.cimple.compiler.CompilerParams;
import com.github.idemura.cimple.compiler.codegen.NoopCodeGenerator;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CimpleCli implements CompilerParams {
  @Parameter() List<String> files = new ArrayList<>();

  @Parameter(names = {"--codegen"})
  String codeGen = "none";

  @Parameter(names = {"--debug"})
  boolean debug;

  @Parameter(names = {"--debug_print_tokens"})
  boolean debugPrintTokens;

  @Parameter(names = {"--debug_print_ast"})
  boolean debugPrintAst;

  @Override
  public Appendable debugOutput() {
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

  CimpleCli() {}

  void parseCmdLine(String[] args) {
    JCommander.newBuilder().addObject(this).build().parse(args);
  }

  boolean run() {
    for (var fileName : files) {
      var code = readCodeFromFile(fileName);
      var compiler =
          new Compiler(this, System.out, new CliErrorConsumer(), new NoopCodeGenerator());
      compiler.compile(fileName, code);
    }
    return true;
  }

  static String readCodeFromFile(String fileName) {
    try {
      return Files.readString(Paths.get(fileName));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static void main(String[] args) {
    var app = new CimpleCli();
    app.parseCmdLine(args);
    System.exit(app.run() ? 1 : 0);
  }
}
