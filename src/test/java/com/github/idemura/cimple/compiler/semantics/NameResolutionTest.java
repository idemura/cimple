package com.github.idemura.cimple.compiler.semantics;

import static com.github.idemura.cimple.compiler.parser.Parser.parseCode;
import static org.junit.jupiter.api.Assertions.*;

import com.github.idemura.cimple.compiler.Identifier;
import java.util.List;
import org.junit.jupiter.api.Test;

class NameResolutionTest extends AbstractSemanticsTest {
  @Test
  void testPopulateNameMap() {
    var code =
        """
        module test;
        type record R {}
        var x int;
        const y int;
        function f() {}
        function g() {}
        """;
    var module = parseCode(code);
    var semanticAnalyzer = new SemanticAnalyzer(errorConsumer);
    semanticAnalyzer.analyze(module);
    assertEquals(List.of(), errorConsumer.errors());
    var nameMap = semanticAnalyzer.nameMap();
    assertSame(module.findVariable("x"), nameMap.lookupEntity(Identifier.ofEntity("x")));
    assertSame(module.findVariable("y"), nameMap.lookupEntity(Identifier.ofEntity("y")));
    assertSame(module.findFunction("f"), nameMap.lookupEntity(Identifier.ofEntity("f")));
    assertSame(module.findFunction("g"), nameMap.lookupEntity(Identifier.ofEntity("g")));
    assertSame(module.findType("R"), nameMap.lookupType(Identifier.ofType("R")));
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
    sa.analyze(module);
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
    sa.analyze(module);
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
    sa.analyze(module);
    assertEquals(
        List.of("Definition of function 'f' has a name collision with variable defined at 2,5"),
        errorConsumer.errors());
  }

  @Test
  void testDuplicateTypeFailure() {
    var code =
        """
        module test;
        type record R {}
        type record R {}
        """;
    var module = parseCode(code);
    var sa = new SemanticAnalyzer(errorConsumer);
    sa.analyze(module);
    assertEquals(List.of("Duplicate type: 'test~R'. Defined at 2,13."), errorConsumer.errors());
  }
}
