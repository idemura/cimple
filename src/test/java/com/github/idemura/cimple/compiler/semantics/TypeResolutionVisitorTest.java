package com.github.idemura.cimple.compiler.semantics;

import static com.github.idemura.cimple.compiler.parser.Parser.parseCode;
import static org.junit.jupiter.api.Assertions.*;

import com.github.idemura.cimple.compiler.InMemoryErrorConsumer;
import com.github.idemura.cimple.compiler.parser.Keyword;
import java.util.List;
import org.junit.jupiter.api.Test;

class TypeResolutionVisitorTest {
  @Test
  void testResolveNamedTypes() {
    var code =
        """
        module test;

        type record Duration {}

        var x Duration;

        function f(a Duration) Duration {}
        """;

    var errorConsumer = new InMemoryErrorConsumer();
    var module = parseCode(code, errorConsumer);
    var nameMap = new NameMap();
    module.accept(new PreprocessVisitor(nameMap, Keyword.valueList(), errorConsumer));
    module.accept(new TypeResolutionVisitor(nameMap, errorConsumer));

    assertEquals(List.of(), errorConsumer.errors());
    var duration = module.findType("Duration");
    assertSame(duration, module.findVariable("x").typeRef().type());
    assertSame(duration, module.findFunction("f").header().parameters().get(0).typeRef().type());
    assertSame(duration, module.findFunction("f").header().resultType().type());
  }

  @Test
  void testUndefinedType() {
    var code =
        """
        module test;

        var x Missing;
        """;

    var errorConsumer = new InMemoryErrorConsumer();
    var module = parseCode(code, errorConsumer);
    var nameMap = new NameMap();
    module.accept(new PreprocessVisitor(nameMap, Keyword.valueList(), errorConsumer));
    module.accept(new TypeResolutionVisitor(nameMap, errorConsumer));

    assertEquals(List.of("Undefined type: Missing"), errorConsumer.errors());
    assertNull(module.findVariable("x").typeRef().type());
  }
}
