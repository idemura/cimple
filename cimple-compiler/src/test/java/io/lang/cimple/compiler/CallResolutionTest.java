package io.lang.cimple.compiler;

import static io.lang.cimple.compiler.ast.AstUtils.*;
import static org.junit.jupiter.api.Assertions.*;

import io.lang.cimple.compiler.ast.AstBuiltinType;
import io.lang.cimple.compiler.ast.AstCall;
import io.lang.cimple.compiler.ast.AstLocal;
import io.lang.cimple.compiler.ast.AstStringType;
import io.lang.cimple.compiler.ast.AstVariableRef;
import java.util.List;
import org.junit.jupiter.api.Test;

class CallResolutionTest extends AbstractSemanticsTest {
  @Test
  void testNormalizeFunctionHeader() {
    var code =
        """
        module test;
        function f(x int) {}
        """;
    var module = parseCode(code);
    var semanticAnalyzer = new SemanticAnalyzer(errorConsumer);
    semanticAnalyzer.analyze(List.of(module));
    assertEquals(List.of(), errorConsumer.errors());
    var header = module.findFunction("f").header();
    assertEquals(AstBuiltinType.VOID, header.resultType());
    var globalNameMap = semanticAnalyzer.globalNameMap();
    assertSame(
        module.findFunction("f"),
        globalNameMap.lookupFunction("test", module.findFunction("f").signature()));
  }

  @Test
  void testResolveVariableAndFunction() {
    var code =
        """
        module test;
        var x int;
        function f() {
          return x;
        }
        function g() {
          return f();
        }
        """;
    var module = parseCode(code);
    var sa = new SemanticAnalyzer(errorConsumer);
    sa.analyze(List.of(module));
    assertEquals(List.of(), errorConsumer.errors());
    {
      var expr = extractReturnExpression(module.findFunction("f"));
      var variableRef = (AstVariableRef) expr;
      assertSame(module.findVariable("x"), variableRef.variable());
    }
    {
      var expr = extractReturnExpression(module.findFunction("g"));
      var call = (AstCall) expr;
      var functionRef = call.function();
      assertSame(module.findFunction("f"), functionRef.function());
    }
  }

  @Test
  void testResolveVariableAndFunctionInvertOrder() {
    var code =
        """
        module test;
        function g() {
          return f();
        }
        function f() {
          return x;
        }
        var x int;
        """;
    var module = parseCode(code);
    var sa = new SemanticAnalyzer(errorConsumer);
    sa.analyze(List.of(module));
    assertEquals(List.of(), errorConsumer.errors());
    {
      var expr = extractReturnExpression(module.findFunction("g"));
      var call = (AstCall) expr;
      var functionRef = call.function();
      assertSame(module.findFunction("f"), functionRef.function());
    }
    {
      var expr = extractReturnExpression(module.findFunction("f"));
      var variableRef = (AstVariableRef) expr;
      assertSame(module.findVariable("x"), variableRef.variable());
    }
  }

  @Test
  void testCrossModuleFunctionResolution() {
    var clientModule =
        parseCode(
            """
            module client;
            function f() int {
              return server~make();
            }
            """);
    var serverModule =
        parseCode(
            """
            module server;
            function make() int;
            """);
    var sa = new SemanticAnalyzer(errorConsumer);
    sa.analyze(List.of(clientModule, serverModule));
    assertEquals(List.of(), errorConsumer.errors());

    var call = (AstCall) extractReturnExpression(clientModule.findFunction("f"));
    var function = call.function();
    assertSame(serverModule.findFunction("make"), function.function());
    assertEquals(Identifier.of("make").module("server"), function.name());
    assertEquals(AstBuiltinType.INT64, call.type());
  }

  @Test
  void testLinkTimeFunctionDeclarationResolves() {
    var code =
        """
        module test;
        function external(x int) string;
        function f() {
          var s = external(1);
        }
        """;
    var module = parseCode(code);
    var sa = new SemanticAnalyzer(errorConsumer);
    sa.analyze(List.of(module));
    assertEquals(List.of(), errorConsumer.errors());

    var external = module.findFunction("external");
    assertNull(external.block());

    var statements = module.findFunction("f").block().statements();
    var local = (AstLocal) statements.get(0);
    assertEquals(AstStringType.INSTANCE, local.variable().type());
    var call = (AstCall) local.variable().expression().get();
    var function = call.function();
    assertSame(external, function.function());
  }

  @Test
  void testCallResolution() {
    var code =
        """
        module test;
        function f(x int) string {}
        function g() {
          var t = f(5);
        }
        """;
    var module = parseCode(code);
    var sa = new SemanticAnalyzer(errorConsumer);
    sa.analyze(List.of(module));
    assertEquals(List.of(), errorConsumer.errors());
    {
      var block = module.findFunction("g").block();
      var local = (AstLocal) block.statements().get(0);
      assertEquals(Identifier.of("t"), local.variable().name());
      assertEquals(AstStringType.INSTANCE, local.variable().type());
      var call = (AstCall) local.variable().expression().get();
      assertEquals(newFunctionRef("test", "f"), call.function());
      assertEquals(AstStringType.INSTANCE, call.type());
    }
  }

  @Test
  void testCallArityMismatch() {
    var code =
        """
        module test;
        function f(x int) {}
        function g() {
          f();
        }
        """;
    var module = parseCode(code);
    var sa = new SemanticAnalyzer(errorConsumer);
    sa.analyze(List.of(module));
    assertEquals(List.of("Undefined function: 'f()'"), errorConsumer.errors());
  }

  @Test
  void testCallExactTypeMismatch() {
    var code =
        """
        module test;
        function f(x int) {}
        function g() {
          f(true);
        }
        """;
    var module = parseCode(code);
    var sa = new SemanticAnalyzer(errorConsumer);
    sa.analyze(List.of(module));
    assertEquals(List.of("Undefined function: 'f(bool)'"), errorConsumer.errors());
  }
}
