package com.github.idemura.cimple.compiler.semantics;

import static com.github.idemura.cimple.compiler.parser.Parser.parseCode;
import static org.junit.jupiter.api.Assertions.*;

import com.github.idemura.cimple.compiler.InMemoryErrorConsumer;
import com.github.idemura.cimple.compiler.QualifiedName;
import com.github.idemura.cimple.compiler.ast.AstCall;
import com.github.idemura.cimple.compiler.ast.AstEntityRef;
import com.github.idemura.cimple.compiler.ast.AstExpression;
import com.github.idemura.cimple.compiler.ast.AstExpressionStatement;
import com.github.idemura.cimple.compiler.ast.AstFunction;
import com.github.idemura.cimple.compiler.ast.AstTypeRef;
import com.github.idemura.cimple.compiler.parser.Keyword;
import java.util.List;
import org.junit.jupiter.api.Test;

class NameResolutionVisitorTest {
  private static AstExpression extractBodyExpression(AstFunction function) {
    return ((AstExpressionStatement) function.block().statements().get(0)).expression();
  }

  @Test
  void testResolveVariableAndFunction() {
    var code =
        """
        module test;

        var x int;

        function f() {
          x;
        }

        function g() {
          f();
        }
        """;

    var errorConsumer = new InMemoryErrorConsumer();
    var module = parseCode(code, errorConsumer);
    var nameMap = new NameMap();
    module.accept(new PreprocessVisitor(nameMap, Keyword.valueList(), errorConsumer));
    module.accept(new NameResolutionVisitor(nameMap, errorConsumer));

    assertEquals(List.of(), errorConsumer.errors());
    {
      var expr = extractBodyExpression(module.findFunction("f"));
      var entityRef = (AstEntityRef) expr;
      assertSame(module.findVariable("x"), entityRef.entity());
    }
    {
      var expr = extractBodyExpression(module.findFunction("g"));
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
          f();
        }

        function f() {
          x;
        }

        var x int;
        """;

    var errorConsumer = new InMemoryErrorConsumer();
    var module = parseCode(code, errorConsumer);
    var nameMap = new NameMap();
    module.accept(new PreprocessVisitor(nameMap, Keyword.valueList(), errorConsumer));
    module.accept(new NameResolutionVisitor(nameMap, errorConsumer));

    assertEquals(List.of(), errorConsumer.errors());
    {
      var expr = extractBodyExpression(module.findFunction("g"));
      var call = (AstCall) expr;
      var entityRef = (AstEntityRef) call.function();
      assertSame(module.findFunction("f"), entityRef.entity());
    }
    {
      var expr = extractBodyExpression(module.findFunction("f"));
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
          d:toMillis();
        }
        """;

    var errorConsumer = new InMemoryErrorConsumer();
    var module = parseCode(code, errorConsumer);
    var nameMap = new NameMap();
    module.accept(new PreprocessVisitor(nameMap, Keyword.valueList(), errorConsumer));
    module.accept(new NameResolutionVisitor(nameMap, errorConsumer));

    assertEquals(List.of(), errorConsumer.errors());

    {
      var expr = extractBodyExpression(module.findFunction("f"));
      var call = (AstCall) expr;
      var function = (AstEntityRef) call.function();
      assertSame(module.findReceiverFunction("Duration", "toMillis"), function.entity());
      assertEquals(new QualifiedName("test", "toMillis"), function.name());

      assertEquals(1, call.arguments().size());
      var receiver = (AstEntityRef) call.arguments().get(0);
      assertEquals(new QualifiedName("d"), receiver.name());
      assertSame(module.findFunction("f").header().parameters().get(0), receiver.entity());
    }
    {
      var function = module.findReceiverFunction("Duration", "toMillis");
      assertEquals(AstTypeRef.ofName("test", "Duration"), function.header().receiverType());
    }
  }
}
