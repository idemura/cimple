package com.github.idemura.cimple.compiler.semantics;

import static org.junit.jupiter.api.Assertions.*;

import com.github.idemura.cimple.compiler.ast.AstBuiltinType;
import com.github.idemura.cimple.compiler.ast.AstCall;
import com.github.idemura.cimple.compiler.ast.AstEntityRef;
import com.github.idemura.cimple.compiler.ast.AstExpression;
import com.github.idemura.cimple.compiler.ast.AstFunction;
import com.github.idemura.cimple.compiler.ast.AstLocal;
import com.github.idemura.cimple.compiler.ast.AstModule;
import com.github.idemura.cimple.compiler.ast.AstRecordType;
import com.github.idemura.cimple.compiler.ast.AstReturn;
import com.github.idemura.cimple.compiler.ast.AstAliasType;
import java.util.List;
import org.junit.jupiter.api.Test;

class AliasTest extends AbstractSemanticsTest {
  private static AstAliasType alias(AstModule module, String name) {
    return (AstAliasType) module.findType(name);
  }

  private static AstLocal firstLocal(AstFunction function) {
    return (AstLocal) function.block().statements().get(0);
  }

  private static AstExpression returnExpression(AstFunction function) {
    for (var statement : function.block().statements()) {
      if (statement instanceof AstReturn returnStatement) {
        return returnStatement.expression().get();
      }
    }
    throw new AssertionError("Return statement is missing");
  }

  private static AstCall returnCall(AstFunction function) {
    return (AstCall) returnExpression(function);
  }

  @Test
  void testAliasChainCollapses() {
    var code =
        """
        module test;
        type record Duration {}
        type alias Time Duration;
        type alias Span Time;
        var global Span;
        function f(value Span) Span {
          var local Span;
          return value;
        }
        """;
    var module = parseCode(code);
    var semanticAnalyzer = new SemanticAnalyzer(errorConsumer);
    semanticAnalyzer.analyze(List.of(module));
    assertEquals(List.of(), errorConsumer.errors());

    var duration = module.findType("Duration");
    assertSame(duration, alias(module, "Time").targetType());
    assertSame(duration, alias(module, "Span").targetType());
    assertSame(duration, semanticAnalyzer.nameMap().lookupType(alias(module, "Span").name()));
    assertSame(duration, module.findVariable("global").type());

    var function = module.findFunction("f");
    assertSame(duration, function.header().parameters().get(0).type());
    assertSame(duration, function.header().resultType());
    assertSame(duration, firstLocal(function).variable().type());

    var value = (AstEntityRef) returnExpression(function);
    assertSame(function.header().parameters().get(0), value.entity());
  }

  @Test
  void testQualifiedAliasTarget() {
    var appModule =
        parseCode(
            """
            module app;
            type alias External common~Duration;
            function f(value External) External {
              return value;
            }
            """);
    var commonModule =
        parseCode(
            """
            module common;
            type record Duration {}
            """);
    new SemanticAnalyzer(errorConsumer).analyze(List.of(appModule, commonModule));
    assertEquals(List.of(), errorConsumer.errors());

    var duration = commonModule.findType("Duration");
    var function = appModule.findFunction("f");
    assertSame(duration, alias(appModule, "External").targetType());
    assertSame(duration, function.header().parameters().get(0).type());
    assertSame(duration, function.header().resultType());
  }

  @Test
  void testAliasInTypeContexts() {
    var code =
        """
        module test;
        type record Duration {
          var seconds int;
        }
        type alias Time Duration;
        type alias Span Time;
        type record Event {
          var duration Span;
        }
        function Span.toSeconds(this) int {
          return this.seconds;
        }
        function f(value Span) int {
          var local Span;
          return value.toSeconds();
        }
        """;
    var module = parseCode(code);
    new SemanticAnalyzer(errorConsumer).analyze(List.of(module));
    assertEquals(List.of(), errorConsumer.errors());

    var duration = module.findType("Duration");
    var event = (AstRecordType) module.findType("Event");
    assertSame(duration, event.fields().get(0).type());

    var method = module.findMethod("Duration", "toSeconds");
    assertSame(duration, method.header().objectType());
    assertSame(duration, method.header().parameters().get(0).type());

    var function = module.findFunction("f");
    assertSame(duration, function.header().parameters().get(0).type());
    assertSame(duration, firstLocal(function).variable().type());

    var call = returnCall(function);
    var functionRef = (AstEntityRef) call.function();
    assertSame(method, functionRef.entity());
    assertEquals(AstBuiltinType.INT64, call.type());
  }

  @Test
  void testUndefinedAliasTarget() {
    var code =
        """
        module test;
        type alias Missing NotDefined;
        """;
    var module = parseCode(code);
    new SemanticAnalyzer(errorConsumer).analyze(List.of(module));
    assertEquals(List.of("Undefined type: 'NotDefined'"), errorConsumer.errors());
  }

  @Test
  void testCircularAlias() {
    var code =
        """
        module test;
        type alias Loop Loop;
        """;
    var module = parseCode(code);
    new SemanticAnalyzer(errorConsumer).analyze(List.of(module));
    assertEquals(List.of("Circular type alias: 'test~Loop'"), errorConsumer.errors());
  }
}
