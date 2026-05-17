package com.github.idemura.cimple.compiler.semantics;

import static com.github.idemura.cimple.compiler.ast.AstUtils.*;
import static org.junit.jupiter.api.Assertions.*;

import com.github.idemura.cimple.compiler.ast.AstBuiltinType;
import java.util.List;
import org.junit.jupiter.api.Test;

class TypeResolutionTest extends AbstractSemanticsTest {
  @Test
  void testArrayTypeResolution() {
    var code =
        """
        module test;
        var a int[];
        var ap int[]*;
        var apa int[]*[];
        """;
    var module = parseCode(code);
    new SemanticAnalyzer(errorConsumer).analyze(module);
    assertEquals(List.of(), errorConsumer.errors());
    assertEquals(arrayType(AstBuiltinType.INT64), module.findVariable("a").type());
    assertEquals(arrayType(pointerType(AstBuiltinType.INT64)), module.findVariable("ap").type());
    assertEquals(
        arrayType(pointerType(arrayType(AstBuiltinType.INT64))), module.findVariable("apa").type());
  }
}
