package io.lang.cimple.cli;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.common.collect.ImmutableList;
import io.lang.cimple.compiler.Compiler;
import io.lang.cimple.compiler.CompilerParams;
import io.lang.cimple.compiler.ErrorConsumer.Mode;
import io.lang.cimple.compiler.SourceCode;
import io.lang.cimple.compiler.codegen.CodeGenerator;
import io.lang.cimple.compiler.codegen.NoopCodeGenerator;
import io.lang.cimple.compiler.codegen.c.CCodeGenerator;
import io.lang.cimple.compiler.codegen.c.CCodeGeneratorParams;
import io.lang.cimple.compiler.codegen.c.CStandard;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CliDriver {
  @Parameter() List<String> files = new ArrayList<>();

  @Parameter(names = {"--codegen"})
  String codeGen = "none";

  @Parameter(names = {"--c_standard"})
  CStandard cStandard = CStandard.def();

  @Parameter(
      names = {"--c_mangle_module_name"},
      arity = 1)
  boolean cMangleModuleName = true;

  @Parameter(
      names = {"--c_output_preamble"},
      arity = 1)
  boolean cOutputPreamble = true;

  @Parameter(names = {"--debug"})
  boolean debug;

  @Parameter(names = {"--debug_print_tokens"})
  boolean printTokens;

  @Parameter(names = {"--debug_print_ast"})
  boolean printAst;

  @Parameter(names = {"--indent"})
  int indent;

  CliDriver() {}

  void parseCmdLine(String[] args) {
    JCommander.newBuilder().addObject(this).build().parse(args);
  }

  List<String> getFilesList() {
    var builder = new ImmutableList.Builder<String>();
    builder.add("lib/_builtin.ci");
    builder.addAll(files);
    return builder.build();
  }

  boolean run() {
    var errorConsumer = new CliErrorConsumer();
    errorConsumer.enable(Mode.PRINT_LEVEL);
    errorConsumer.enable(Mode.PRINT_LOCATION);
    var compiler = new Compiler(compilerParams(), errorConsumer, codeGenerator());
    var sourceCodeList =
        getFilesList().stream().map(CliDriver::readSourceCodeFromFile).collect(toImmutableList());
    return compiler.compile(sourceCodeList);
  }

  private CompilerParams compilerParams() {
    return CompilerParams.builder()
        .indent(indent == 0 ? 2 : indent)
        .printTokens(debug && printTokens)
        .printAst(debug && printAst)
        .build();
  }

  private CodeGenerator codeGenerator() {
    return switch (codeGen) {
      case "none" -> new NoopCodeGenerator();
      case "c" ->
          new CCodeGenerator(
              CCodeGeneratorParams.builder()
                  .standard(cStandard)
                  .mangleModuleName(cMangleModuleName)
                  .outputPreamble(cOutputPreamble)
                  .build(),
              System.out);
      default -> throw new IllegalArgumentException("Unknown code generator: " + codeGen);
    };
  }

  static SourceCode readSourceCodeFromFile(String fileName) {
    try {
      return new SourceCode(Files.readString(Paths.get(fileName)), fileName);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static void main(String[] args) {
    var app = new CliDriver();
    app.parseCmdLine(args);
    System.exit(app.run() ? 0 : 1);
  }
}
