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
import com.github.idemura.cimple.compiler.parser.Keyword;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class NameResolutionVisitorTest {
  private static AstExpression extractBodyExpression(AstFunction function) {
    return ((AstExpressionStatement) function.getBlock().statements().get(0)).getExpression();
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

    assertEquals(List.of(), errorConsumer.getErrors());
    {
      var expr = extractBodyExpression(module.findFunction("f"));
      var entityRef = (AstEntityRef) expr;
      assertSame(module.findVariable("x"), entityRef.getEntity());
    }
    {
      var expr = extractBodyExpression(module.findFunction("g"));
      var call = (AstCall) expr;
      var entityRef = (AstEntityRef) call.getFunction();
      assertSame(module.findFunction("f"), entityRef.getEntity());
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

    assertEquals(List.of(), errorConsumer.getErrors());
    {
      var expr = extractBodyExpression(module.findFunction("g"));
      var call = (AstCall) expr;
      var entityRef = (AstEntityRef) call.getFunction();
      assertSame(module.findFunction("f"), entityRef.getEntity());
    }
    {
      var expr = extractBodyExpression(module.findFunction("f"));
      var entityRef = (AstEntityRef) expr;
      assertSame(module.findVariable("x"), entityRef.getEntity());
    }
  }

  @Disabled
  @Test
  void testReceiverFunctions() {
    var code =
        """
        module test;

        type record Duration {
          var seconds int;
        }

        function Duration:toMillis(this) {
          return this.seconds * 1000;
        }
        # function Duration:add(this, d Duration) {
        #   this.seconds += d.seconds;
        # }
        """;

    var errorConsumer = new InMemoryErrorConsumer();
    var module = parseCode(code, errorConsumer);
    var nameMap = new NameMap();
    module.accept(new PreprocessVisitor(nameMap, Keyword.valueList(), errorConsumer));
    module.accept(new NameResolutionVisitor(nameMap, errorConsumer));

    assertEquals(List.of(), errorConsumer.getErrors());

    {
      var expr =
          extractBodyExpression(
              nameMap.lookupReceiverFunction(new QualifiedName("Duration"), "toMillis"));
      var call = (AstCall) expr;
      var entityRef = (AstEntityRef) call.getFunction();
      assertSame(module.findFunction("f"), entityRef.getEntity());
    }
  }
}
