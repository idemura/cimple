package io.lang.cimple.compiler;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class TypeResolutionTest extends AbstractTest {
  @Test
  void testArrayTypeResolution() {
    var code =
        """
        module test;
        var a int[];
        var ap int[]*;
        var pa int*[];
        """;
    var module = parseCode(code);
    new SemanticAnalyzer(errorConsumer).analyze(List.of(module));
    assertEquals(List.of(), errorConsumer.errors());
    assertEquals(new AstArrayType(AstBuiltinType.INT64), module.findVariable("a").type());
    assertEquals(
        new AstPointerType(new AstArrayType(AstBuiltinType.INT64)),
        module.findVariable("ap").type());
    assertEquals(
        new AstArrayType(new AstPointerType(AstBuiltinType.INT64)),
        module.findVariable("pa").type());
  }
}
