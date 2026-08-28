package com.github.idemura.cimple.compiler;

import com.github.idemura.cimple.compiler.ast.AstModule;

abstract class AbstractSemanticsTest {
  final InMemoryErrorConsumer errorConsumer = new InMemoryErrorConsumer();

  AstModule parseCode(String code) {
    return Parser.parseCode(code, errorConsumer);
  }
}
