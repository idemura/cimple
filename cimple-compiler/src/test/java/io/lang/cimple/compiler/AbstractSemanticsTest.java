package io.lang.cimple.compiler;

abstract class AbstractSemanticsTest {
  final ErrorConsumer errorConsumer = new ErrorConsumer();

  AstModule parseCode(String code) {
    return Parser.parseCode(code, errorConsumer);
  }
}
