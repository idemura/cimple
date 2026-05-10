package com.github.idemura.cimple.compiler.semantics;

import com.github.idemura.cimple.compiler.InMemoryErrorConsumer;

abstract class AbstractSemanticsTest {
  final InMemoryErrorConsumer errorConsumer = new InMemoryErrorConsumer();
}
