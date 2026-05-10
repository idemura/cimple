package com.github.idemura.cimple.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.github.idemura.cimple.compiler.Compiler;
import com.github.idemura.cimple.compiler.CompilerParams;
import com.github.idemura.cimple.compiler.ErrorConsumer.Mode;
import com.github.idemura.cimple.compiler.codegen.NoopCodeGenerator;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CliDriver implements CompilerParams {
  @Parameter() List<String> files = new ArrayList<>();

  @Parameter(names = {"--codegen"})
  String codeGen = "none";

  @Parameter(names = {"--debug"})
  boolean debug;

  @Parameter(names = {"--debug_print_tokens"})
  boolean printTokens = CompilerParams.super.printTokens();

  @Parameter(names = {"--debug_print_ast"})
  boolean printAst = CompilerParams.super.printAst();

  @Parameter(names = {"--indent"})
  int indent = CompilerParams.super.indent();

  @Override
  public Appendable debugOutput() {
    return System.err;
  }

  @Override
  public int indent() {
    return indent == 0 ? CompilerParams.super.indent() : indent;
  }

  @Override
  public boolean printTokens() {
    return debug && printTokens;
  }

  @Override
  public boolean printAst() {
    return debug && printAst;
  }

  CliDriver() {}

  void parseCmdLine(String[] args) {
    JCommander.newBuilder().addObject(this).build().parse(args);
  }

  boolean run() {
    var success = true;
    var errorConsumer = new CliErrorConsumer();
    errorConsumer.enable(Mode.PRINT_LEVEL);
    errorConsumer.enable(Mode.PRINT_LOCATION);
    var compiler = new Compiler(this, System.out, errorConsumer, new NoopCodeGenerator());
    for (var fileName : files) {
      var code = readCodeFromFile(fileName);
      if (!compiler.compile(fileName, code)) {
        success = false;
      }
    }
    return success;
  }

  static String readCodeFromFile(String fileName) {
    try {
      return Files.readString(Paths.get(fileName));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static void main(String[] args) {
    var app = new CliDriver();
    app.parseCmdLine(args);
    System.exit(app.run() ? 1 : 0);
  }
}
