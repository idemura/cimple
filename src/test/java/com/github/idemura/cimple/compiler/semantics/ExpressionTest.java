package com.github.idemura.cimple.compiler.semantics;

import static org.junit.jupiter.api.Assertions.*;

import com.github.idemura.cimple.compiler.ast.AstBuiltinType;
import com.github.idemura.cimple.compiler.ast.AstCall;
import com.github.idemura.cimple.compiler.ast.AstCompoundAssign;
import com.github.idemura.cimple.compiler.ast.AstEntityRef;
import com.github.idemura.cimple.compiler.ast.AstExpressionStatement;
import com.github.idemura.cimple.compiler.ast.AstFunction;
import com.github.idemura.cimple.compiler.ast.AstLocal;
import com.github.idemura.cimple.compiler.ast.AstStatement;
import java.util.List;
import org.junit.jupiter.api.Test;

class ExpressionTest extends AbstractSemanticsTest {
  private static void assertOperator(AstStatement statement, AstFunction function) {
    var call = (AstCall) ((AstLocal) statement).variable().expression().get();
    var functionRef = (AstEntityRef) call.function();
    assertSame(function, functionRef.entity());
    assertEquals(AstBuiltinType.INT64, call.type());
  }

  private static void assertComparisonOperator(AstStatement statement, AstFunction function) {
    var call = (AstCall) ((AstLocal) statement).variable().expression().get();
    var functionRef = (AstEntityRef) call.function();
    assertSame(function, functionRef.entity());
    assertEquals(AstBuiltinType.BOOL, call.type());
  }

  private static void assertCompoundOperator(Object statement, AstFunction function) {
    var expr = ((AstExpressionStatement) statement).expression().get();
    var assign = (AstCompoundAssign) expr;
    assertSame(function, assign.operation().entity());
    assertEquals(AstBuiltinType.INT64, assign.type());
  }

  @Test
  void testExpression() {
    var code =
        """
        module test;
        type record Duration {
          var seconds int;
        }
        function f(d Duration) {
          var x = 1 + 2;
          var y = d.seconds;
        }
        """;
    var module = parseCode(code);
    var sa = new SemanticAnalyzer(errorConsumer);
    sa.analyze(List.of(module));
    assertEquals(List.of(), errorConsumer.errors());
  }

  @Test
  void testArithmeticOperatorsResolveToI64Builtins() {
    var code =
        """
        module test;
        function f() {
          var add = 1 + 2;
          var sub = 1 - 2;
          var mul = 1 * 2;
          var div = 1 / 2;
          var mod = 1 % 2;
        }
        """;
    var module = parseCode(code);
    var sa = new SemanticAnalyzer(errorConsumer);
    sa.analyze(List.of(module));
    assertEquals(List.of(), errorConsumer.errors());

    var statements = module.findFunction("f").block().statements();
    assertEquals(5, statements.size());
    assertOperator(statements.get(0), BuiltinFunctions.ADD_I64);
    assertOperator(statements.get(1), BuiltinFunctions.SUB_I64);
    assertOperator(statements.get(2), BuiltinFunctions.MUL_I64);
    assertOperator(statements.get(3), BuiltinFunctions.DIV_I64);
    assertOperator(statements.get(4), BuiltinFunctions.MOD_I64);
  }

  @Test
  void testComparisonOperatorsResolveToI64Builtins() {
    var code =
        """
        module test;
        function f() {
          var lt = 1 < 2;
          var le = 1 <= 2;
          var gt = 1 > 2;
          var ge = 1 >= 2;
          var eq = 1 == 2;
          var ne = 1 != 2;
        }
        """;
    var module = parseCode(code);
    var sa = new SemanticAnalyzer(errorConsumer);
    sa.analyze(List.of(module));
    assertEquals(List.of(), errorConsumer.errors());

    var statements = module.findFunction("f").block().statements();
    assertEquals(6, statements.size());
    assertComparisonOperator(statements.get(0), BuiltinFunctions.LT_I64);
    assertComparisonOperator(statements.get(1), BuiltinFunctions.LE_I64);
    assertComparisonOperator(statements.get(2), BuiltinFunctions.GT_I64);
    assertComparisonOperator(statements.get(3), BuiltinFunctions.GE_I64);
    assertComparisonOperator(statements.get(4), BuiltinFunctions.EQ_I64);
    assertComparisonOperator(statements.get(5), BuiltinFunctions.NE_I64);
  }

  @Test
  void testCompoundAssignmentOperatorsResolveToI64Builtins() {
    var code =
        """
        module test;
        function f() {
          var x = 1;
          x += 2;
          x -= 3;
          x *= 4;
          x /= 5;
          x %= 6;
        }
        """;
    var module = parseCode(code);
    var sa = new SemanticAnalyzer(errorConsumer);
    sa.analyze(List.of(module));
    assertEquals(List.of(), errorConsumer.errors());

    var statements = module.findFunction("f").block().statements();
    assertEquals(6, statements.size());
    assertCompoundOperator(statements.get(1), BuiltinFunctions.ADD_I64);
    assertCompoundOperator(statements.get(2), BuiltinFunctions.SUB_I64);
    assertCompoundOperator(statements.get(3), BuiltinFunctions.MUL_I64);
    assertCompoundOperator(statements.get(4), BuiltinFunctions.DIV_I64);
    assertCompoundOperator(statements.get(5), BuiltinFunctions.MOD_I64);
  }

  @Test
  void testInvalidFieldAccess() {
    var code =
        """
        module test;
        type record Duration {
          var seconds int;
        }
        function f() {
          var x = 1.seconds;
          var d Duration;
          var y = d.millis;
        }
        """;
    var module = parseCode(code);
    var sa = new SemanticAnalyzer(errorConsumer);
    sa.analyze(List.of(module));
    assertEquals(
        List.of(
            "Field access requires a record, got 'int64'",
            "Undefined field 'millis' in record 'test~Duration'"),
        errorConsumer.errors());
  }

}
