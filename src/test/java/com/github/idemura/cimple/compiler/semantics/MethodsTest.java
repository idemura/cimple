package com.github.idemura.cimple.compiler.semantics;

import static com.github.idemura.cimple.compiler.ast.AstUtils.*;
import static org.junit.jupiter.api.Assertions.*;

import com.github.idemura.cimple.compiler.Identifier;
import com.github.idemura.cimple.compiler.ast.AstBuiltinType;
import com.github.idemura.cimple.compiler.ast.AstCall;
import com.github.idemura.cimple.compiler.ast.AstEntityRef;
import com.github.idemura.cimple.compiler.ast.AstExpressionStatement;
import com.github.idemura.cimple.compiler.ast.AstFunction;
import com.github.idemura.cimple.compiler.ast.AstType;
import java.util.List;
import org.junit.jupiter.api.Test;

class MethodsTest extends AbstractSemanticsTest {
  private static AstCall returnCall(AstFunction function) {
    return (AstCall) extractReturnExpression(function);
  }

  private static AstCall statementCall(AstFunction function) {
    var statement = (AstExpressionStatement) function.block().statements().get(0);
    return (AstCall) statement.expression().get();
  }

  private static void assertMethodCall(
      AstFunction caller, AstFunction method, AstType expectedType) {
    var call = returnCall(caller);
    var function = (AstEntityRef) call.function();
    assertSame(method, function.entity());
    assertEquals(expectedType, call.type());
    assertEquals(1, call.arguments().size());

    var object = (AstEntityRef) call.arguments().get(0);
    assertSame(caller.header().parameters().get(0), object.entity());
  }

  @Test
  void testRecordMethod() {
    var code =
        """
        module test;
        type record Duration {
          var seconds int;
        }
        function Duration.toMillis(this) int {
          return this.seconds;
        }
        function f(d Duration) int {
          return d.toMillis();
        }
        """;
    var module = parseCode(code);
    new SemanticAnalyzer(errorConsumer).analyze(List.of(module));
    assertEquals(List.of(), errorConsumer.errors());

    var method = module.findMethod("Duration", "toMillis");
    assertSame(module.findType("Duration"), method.header().objectType());
    assertMethodCall(module.findFunction("f"), method, AstBuiltinType.INT64);
  }

  @Test
  void testUnionMethod() {
    var code =
        """
        module test;
        type union Maybe {
          None;
          Some(int);
        }
        function Maybe.isSome(this) bool {
          return true;
        }
        function f(m Maybe) bool {
          return m.isSome();
        }
        """;
    var module = parseCode(code);
    new SemanticAnalyzer(errorConsumer).analyze(List.of(module));
    assertEquals(List.of(), errorConsumer.errors());

    var method = module.findMethod("Maybe", "isSome");
    assertSame(module.findType("Maybe"), method.header().objectType());
    assertMethodCall(module.findFunction("f"), method, AstBuiltinType.BOOL);
  }

  @Test
  void testBuiltinTypeMethod() {
    var code =
        """
        module test;
        function int64.isPositive(this) bool {
          return this > 0;
        }
        function f(n int64) bool {
          return n.isPositive();
        }
        """;
    var module = parseCode(code);
    new SemanticAnalyzer(errorConsumer).analyze(List.of(module));
    assertEquals(List.of(), errorConsumer.errors());

    var method = module.findMethod("int64", "isPositive");
    assertEquals(new Identifier("test", "int64", "isPositive"), method.name());
    assertEquals(AstBuiltinType.INT64, method.header().objectType());
    assertMethodCall(module.findFunction("f"), method, AstBuiltinType.BOOL);
  }

  @Test
  void testAliasMethods() {
    var code =
        """
        module test;
        function int.abs(n) {
        }
        function f(n int) {
          n.abs();
        }
        """;
    var module = parseCode(code);
    new SemanticAnalyzer(errorConsumer).analyze(List.of(module));
    assertEquals(List.of(), errorConsumer.errors());

    var method = module.findMethod("int64", "abs");
    assertEquals(new Identifier("test", "int64", "abs"), method.name());
    assertEquals(AstBuiltinType.INT64, method.header().objectType());
    assertEquals(AstBuiltinType.INT64, method.header().parameters().get(0).type());

    var call = statementCall(module.findFunction("f"));
    var function = (AstEntityRef) call.function();
    assertSame(method, function.entity());
    assertEquals(AstBuiltinType.VOID, call.type());

    var object = (AstEntityRef) call.arguments().get(0);
    assertSame(module.findFunction("f").header().parameters().get(0), object.entity());
  }
}
