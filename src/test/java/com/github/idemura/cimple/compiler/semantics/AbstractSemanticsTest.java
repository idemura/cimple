package com.github.idemura.cimple.compiler.semantics;

import com.github.idemura.cimple.compiler.InMemoryErrorConsumer;
import com.github.idemura.cimple.compiler.ast.AstModule;
import com.github.idemura.cimple.compiler.parser.Parser;

abstract class AbstractSemanticsTest {
  final InMemoryErrorConsumer errorConsumer = new InMemoryErrorConsumer();

  AstModule parseCode(String code) {
    return Parser.parseCode(code, errorConsumer);
  }
}
