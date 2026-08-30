package io.lang.cimple.compiler;

import static io.lang.cimple.compiler.ast.AstUtils.*;
import static org.junit.jupiter.api.Assertions.*;

import io.lang.cimple.compiler.ast.AstBuiltinType;
import io.lang.cimple.compiler.ast.AstCall;
import io.lang.cimple.compiler.ast.AstEntityRef;
import io.lang.cimple.compiler.ast.AstLocal;
import io.lang.cimple.compiler.ast.AstStringType;
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
        module.findFunction("f"), globalNameMap.lookupEntity(newEntityRef("test", "f").name()));
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
      var entityRef = (AstEntityRef) expr;
      assertSame(module.findVariable("x"), entityRef.entity());
    }
    {
      var expr = extractReturnExpression(module.findFunction("g"));
      var call = (AstCall) expr;
      var entityRef = (AstEntityRef) call.function();
      assertSame(module.findFunction("f"), entityRef.entity());
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
      var entityRef = (AstEntityRef) call.function();
      assertSame(module.findFunction("f"), entityRef.entity());
    }
    {
      var expr = extractReturnExpression(module.findFunction("f"));
      var entityRef = (AstEntityRef) expr;
      assertSame(module.findVariable("x"), entityRef.entity());
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
    var function = (AstEntityRef) call.function();
    assertSame(serverModule.findFunction("make"), function.entity());
    assertEquals(Identifier.ofEntity("make").withModule("server"), function.name());
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
    var function = (AstEntityRef) call.function();
    assertSame(external, function.entity());
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
      assertEquals(Identifier.ofEntity("t"), local.variable().name());
      assertEquals(AstStringType.INSTANCE, local.variable().type());
      var call = (AstCall) local.variable().expression().get();
      assertEquals(newEntityRef("test", "f"), call.function());
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
    assertEquals(List.of("Function 'test~f' expects 1 arguments, got 0"), errorConsumer.errors());
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
    assertEquals(
        List.of("Argument 0 of function 'test~f' has type 'bool', expected 'int64'"),
        errorConsumer.errors());
  }
}
