package io.lang.cimple.compiler;

import static io.lang.cimple.compiler.AstTreeUtils.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class FunctionGenericsTest extends AbstractTest {
  @Test
  void testGenericFunctionResolution() {
    var code =
        """
        module test;
        generic (T) function first(a T[]) T;
        function f(values int[]) int {
          return first(values);
        }
        """;
    var module = parseCode(code);
    new SemanticAnalyzer(errorConsumer).analyze(List.of(module));

    assertEquals(List.of(), errorConsumer.errors());
    var first = module.findFunction("first");
    var call = (AstCall) extractReturnExpression(module.findFunction("f"));
    assertSame(first, call.function().function());
    assertEquals(AstBuiltinType.INT64, call.deducedWildcards().get(first.wildcards().get(0)));
    assertEquals(AstBuiltinType.INT64, call.type());
  }

  @Test
  void testWildcardTemporarilyShadowsModuleType() {
    var code =
        """
        module test;
        type struct T {}
        generic (T) function first(a T[]) T;
        function identity(a T) T;
        """;
    var module = parseCode(code);
    new SemanticAnalyzer(errorConsumer).analyze(List.of(module));

    assertEquals(List.of(), errorConsumer.errors());
    var wildcard = module.findFunction("first").wildcards().get(0);
    var arrayType = (AstArrayType) module.findFunction("first").parameters().get(0).type();
    assertSame(wildcard, arrayType.baseType());
    assertSame(module.findType("T"), module.findFunction("identity").parameters().get(0).type());
  }

  @Test
  void testGenericParameterMustBeUsedInParameterTypes() {
    var code =
        """
        module test;
        generic (T) function make() T;
        generic (T, U) function convert(x T) U;
        generic (T) function bodyOnly(x int) {
          var value T;
        }
        """;
    var module = parseCode(code);
    new SemanticAnalyzer(errorConsumer).analyze(List.of(module));

    assertEquals(
        List.of(
            "Generic parameter 'T' must be used in function parameter types",
            "Generic parameter 'U' must be used in function parameter types",
            "Generic parameter 'T' must be used in function parameter types"),
        errorConsumer.errors());
  }

  @Test
  void testGenericPointerTypesAreUnsupported() {
    var code =
        """
        module test;
        generic (T) function p(a T*);
        """;
    var module = parseCode(code);

    assertThrows(
        UnsupportedOperationException.class,
        () -> new SemanticAnalyzer(errorConsumer).analyze(List.of(module)));
  }

  @Test
  void testGenericAndConcreteArraySignaturesCollide() {
    var code =
        """
        module test;
        function f(a int[]);
        generic (T) function f(a T[]);
        """;
    var module = parseCode(code);
    new SemanticAnalyzer(errorConsumer).analyze(List.of(module));

    assertEquals(
        List.of("Definition of function 'f' has a name collision with function defined at 2,10"),
        errorConsumer.errors());
  }

  @Test
  void testConcreteArrayOverloadsAreAllowed() {
    var code =
        """
        module test;
        function f(a int[]);
        function f(a bool[]);
        """;
    var module = parseCode(code);
    new SemanticAnalyzer(errorConsumer).analyze(List.of(module));

    assertEquals(List.of(), errorConsumer.errors());
  }
}
