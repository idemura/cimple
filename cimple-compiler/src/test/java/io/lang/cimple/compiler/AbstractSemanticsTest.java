package io.lang.cimple.compiler;

import io.lang.cimple.compiler.ast.AstModule;

abstract class AbstractSemanticsTest {
  final ErrorConsumer errorConsumer = new ErrorConsumer();

  AstModule parseCode(String code) {
    return Parser.parseCode(code, errorConsumer);
  }
}
