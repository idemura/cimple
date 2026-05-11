package com.github.idemura.cimple.compiler.semantics;

import static com.github.idemura.cimple.compiler.ast.AstUtils.*;
import static com.github.idemura.cimple.compiler.parser.Parser.parseCode;
import static org.junit.jupiter.api.Assertions.*;

import com.github.idemura.cimple.compiler.Identifier;
import com.github.idemura.cimple.compiler.ast.AstBuiltinType;
import com.github.idemura.cimple.compiler.ast.AstCall;
import com.github.idemura.cimple.compiler.ast.AstEntityRef;
import com.github.idemura.cimple.compiler.ast.AstLocal;
import java.util.List;
import org.junit.jupiter.api.Test;

class CallResolutionTest extends AbstractSemanticsTest {
  @Test
  void testNormalizeFunctionHeader() {
    var code =
        """
        module test;
        type record Duration {}
        function Duration:toMillis(x int, this) {}
        function f(x int) {}
        """;
    var module = parseCode(code, errorConsumer);
    var semanticAnalyzer = new SemanticAnalyzer(errorConsumer);
    semanticAnalyzer.analyze(module);
    assertEquals(List.of(), errorConsumer.errors());
    {
      var header = module.findReceiverFunction("Duration", "toMillis").header();
      var receiverType = newRecordType("test", "Duration");
      assertEquals(receiverType, header.receiverType());
      assertEquals(1, header.receiverIndex());
      assertEquals(receiverType, header.parameters().get(1).type());
      assertEquals(AstBuiltinType.VOID, header.resultType());
    }
    {
      var header = module.findFunction("f").header();
      assertEquals(-1, header.receiverIndex());
      assertEquals(AstBuiltinType.VOID, header.resultType());
    }
    var nameMap = semanticAnalyzer.nameMap();
    assertSame(
        module.findReceiverFunction("Duration", "toMillis"),
        nameMap.lookupReceiverFunction(new Identifier("test", "Duration", "toMillis")));
    assertNull(nameMap.lookupEntity(Identifier.ofEntity("toMillis")));
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
    var module = parseCode(code, errorConsumer);
    new SemanticAnalyzer(errorConsumer).analyze(module);
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
    var module = parseCode(code, errorConsumer);
    new SemanticAnalyzer(errorConsumer).analyze(module);
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
  void testReceiverFunctionResolution() {
    var code =
        """
        module test;
        type record Duration {
          var seconds int;
        }
        function Duration:toMillis(this) int {
          return this.seconds * 1000;
        }
        function f(d Duration) {
          return d:toMillis();
        }
        """;
    var module = parseCode(code, errorConsumer);
    new SemanticAnalyzer(errorConsumer).analyze(module);
    assertEquals(List.of(), errorConsumer.errors());
    {
      var expr = extractReturnExpression(module.findFunction("f"));
      var call = (AstCall) expr;
      var function = (AstEntityRef) call.function();
      assertSame(module.findReceiverFunction("Duration", "toMillis"), function.entity());
      assertEquals(new Identifier("test", "Duration", "toMillis"), function.name());
      assertEquals(1, call.arguments().size());
      var receiver = (AstEntityRef) call.arguments().get(0);
      assertEquals(Identifier.ofEntity("d"), receiver.name());
      assertSame(module.findFunction("f").header().parameters().get(0), receiver.entity());
    }
    {
      var function = module.findReceiverFunction("Duration", "toMillis");
      assertEquals(newRecordType("test", "Duration"), function.header().receiverType());
    }
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
    var module = parseCode(code, errorConsumer);
    new SemanticAnalyzer(errorConsumer).analyze(module);
    assertEquals(List.of(), errorConsumer.errors());
    {
      var block = module.findFunction("g").block();
      var local = (AstLocal) block.statements().get(0);
      assertEquals(Identifier.ofEntity("t"), local.variable().name());
      assertEquals(AstBuiltinType.STRING, local.variable().type());
      var call = (AstCall) local.variable().expression();
      assertEquals(newEntityRef("test", "f"), call.function());
      assertEquals(AstBuiltinType.STRING, call.type());
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
    var module = parseCode(code, errorConsumer);
    new SemanticAnalyzer(errorConsumer).analyze(module);
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
    var module = parseCode(code, errorConsumer);
    new SemanticAnalyzer(errorConsumer).analyze(module);
    assertEquals(
        List.of("Argument 0 of function 'test~f' has type 'bool', expected 'int64'"),
        errorConsumer.errors());
  }
}
