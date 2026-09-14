package io.lang.cimple.compiler;

import com.google.common.collect.ImmutableList;

abstract class AbstractTest {
  final ErrorConsumer errorConsumer = new ErrorConsumer();

  AstModule parseCode(String code) {
    return Parser.parseCode(code, errorConsumer);
  }

  void analyze(String code) {
    var module = Parser.parseCode(code, errorConsumer);
    new SemanticAnalyzer(errorConsumer).analyze(ImmutableList.of(module));
  }
}
