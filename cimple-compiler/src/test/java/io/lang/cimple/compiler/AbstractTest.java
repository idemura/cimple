package io.lang.cimple.compiler;

abstract class AbstractTest {
  final ErrorConsumer errorConsumer = new ErrorConsumer();

  AstModule parseCode(String code) {
    return Parser.parseCode(code, errorConsumer);
  }
}
