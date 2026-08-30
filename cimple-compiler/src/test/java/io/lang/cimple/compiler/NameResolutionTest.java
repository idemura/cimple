package io.lang.cimple.compiler;

import static io.lang.cimple.compiler.ast.AstUtils.extractReturnExpression;
import static org.junit.jupiter.api.Assertions.*;
import io.lang.cimple.compiler.ast.AstCall;
import io.lang.cimple.compiler.ast.AstVariableRef;
import org.junit.jupiter.api.Test;
import java.util.List;

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
    var typeMap = semanticAnalyzer.globalNameMap().collectTypes("test", errorConsumer);
    var localNameMap = semanticAnalyzer.globalNameMap().collectVariables("test", errorConsumer);
    assertSame(module.findVariable("x"), localNameMap.lookupVariable("x"));
    assertSame(module.findVariable("y"), localNameMap.lookupVariable("y"));
    assertSame(
        module.findFunction("f"),
        semanticAnalyzer
            .globalNameMap()
            .lookupFunction("test", module.findFunction("f").signature()));
    assertSame(
        module.findFunction("g"),
        semanticAnalyzer
            .globalNameMap()
            .lookupFunction("test", module.findFunction("g").signature()));
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
  void testFunctionAndVariableCanHaveSameName() {
    var code =
        """
        module test;
        var f int;
        function f() int {
          return 1;
        }
        function g() int {
          return f();
        }
        function h() int {
          return f;
        }
        """;
    var module = parseCode(code);
    var sa = new SemanticAnalyzer(errorConsumer);
    sa.analyze(List.of(module));
    assertEquals(List.of(), errorConsumer.errors());
    {
      var call = (AstCall) extractReturnExpression(module.findFunction("g"));
      var function = call.function();
      assertSame(module.findFunction("f"), function.function());
    }
    {
      var variable = (AstVariableRef) extractReturnExpression(module.findFunction("h"));
      assertSame(module.findVariable("f"), variable.variable());
    }
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
