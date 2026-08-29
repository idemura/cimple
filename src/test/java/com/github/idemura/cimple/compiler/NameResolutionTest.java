package com.github.idemura.cimple.compiler;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class NameResolutionTest extends AbstractSemanticsTest {
  @Test
  void testPopulateNameMap() {
    var code =
        """
        module test;
        type struct R {}
        var x int;
        const y int;
        function f() {}
        function g() {}
        """;
    var module = parseCode(code);
    var semanticAnalyzer = new SemanticAnalyzer(errorConsumer);
    semanticAnalyzer.analyze(List.of(module));
    assertEquals(List.of(), errorConsumer.errors());
    var typeMap = semanticAnalyzer.globalNameMap().collectTypes(module, errorConsumer);
    var localNameMap =
        semanticAnalyzer.globalNameMap().collectFunctionsAndVariables(module, errorConsumer);
    assertSame(module.findVariable("x"), localNameMap.lookupEntity(Identifier.ofEntity("x")));
    assertSame(module.findVariable("y"), localNameMap.lookupEntity(Identifier.ofEntity("y")));
    assertSame(module.findFunction("f"), localNameMap.lookupEntity(Identifier.ofEntity("f")));
    assertSame(module.findFunction("g"), localNameMap.lookupEntity(Identifier.ofEntity("g")));
    assertSame(module.findType("R"), typeMap.get("R"));
  }

  @Test
  void testDuplicateVariableFailure() {
    var code =
        """
        module test;
        var x int;
        const x int;
        """;
    var module = parseCode(code);
    var sa = new SemanticAnalyzer(errorConsumer);
    sa.analyze(List.of(module));
    assertEquals(
        List.of("Definition of variable 'x' has a name collision with variable defined at 2,5"),
        errorConsumer.errors());
  }

  @Test
  void testDuplicateFunctionFailure() {
    var code =
        """
        module test;
        function f() {}
        function f() {}
        """;
    var module = parseCode(code);
    var sa = new SemanticAnalyzer(errorConsumer);
    sa.analyze(List.of(module));
    assertEquals(
        List.of("Definition of function 'f' has a name collision with function defined at 2,10"),
        errorConsumer.errors());
  }

  @Test
  void testFunctionVariableCollisionFailure() {
    var code =
        """
        module test;
        var f int;
        function f() {}
        """;
    var module = parseCode(code);
    var sa = new SemanticAnalyzer(errorConsumer);
    sa.analyze(List.of(module));
    assertEquals(
        List.of("Definition of function 'f' has a name collision with variable defined at 2,5"),
        errorConsumer.errors());
  }

  @Test
  void testDuplicateTypeFailure() {
    var code =
        """
        module test;
        type struct R {}
        type struct R {}
        """;
    var module = parseCode(code);
    var sa = new SemanticAnalyzer(errorConsumer);
    sa.analyze(List.of(module));
    assertEquals(List.of("Duplicate type: 'test~R'. Defined at 2,13."), errorConsumer.errors());
  }
}
