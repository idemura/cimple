package com.github.idemura.cimple.compiler.semantics;

import static com.github.idemura.cimple.compiler.parser.Parser.parseCode;
import static org.junit.jupiter.api.Assertions.*;

import com.github.idemura.cimple.compiler.InMemoryErrorConsumer;
import com.github.idemura.cimple.compiler.ast.AstBoolLiteral;
import com.github.idemura.cimple.compiler.ast.AstDefer;
import com.github.idemura.cimple.compiler.ast.AstExpressionStatement;
import com.github.idemura.cimple.compiler.ast.AstFor;
import com.github.idemura.cimple.compiler.ast.AstFunction;
import com.github.idemura.cimple.compiler.ast.AstIf;
import com.github.idemura.cimple.compiler.ast.AstModule;
import com.github.idemura.cimple.compiler.ast.AstNullLiteral;
import com.github.idemura.cimple.compiler.ast.AstReturn;
import com.github.idemura.cimple.compiler.ast.AstType;
import com.github.idemura.cimple.compiler.ast.AstVariable;
import com.github.idemura.cimple.compiler.ast.AstVariableStatement;
import com.github.idemura.cimple.compiler.parser.Keyword;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.jupiter.api.Test;

class PreprocessVisitorTest {
  private static List<AstFunction> functions(AstModule module) {
    return module.definitions().stream()
        .filter(AstFunction.class::isInstance)
        .map(AstFunction.class::cast)
        .toList();
  }

  private static List<AstType> types(AstModule module) {
    return module.definitions().stream()
        .filter(AstType.class::isInstance)
        .map(AstType.class::cast)
        .toList();
  }

  private static List<AstVariable> variables(AstModule module) {
    return module.definitions().stream()
        .filter(AstVariable.class::isInstance)
        .map(AstVariable.class::cast)
        .toList();
  }

  @Test
  void testRewriteTrueFalseNullLiterals() {
    var code =
        """
        module test;

        function f() {
          if true {
          }
          defer null;
          var x = false;
          for var i = null; true; true {
          }
          return true;
        }
        """;

    var errorConsumer = new InMemoryErrorConsumer();
    var module = parseCode(code, errorConsumer);
    var nameMap = new NameMap();
    module.accept(new PreprocessVisitor(nameMap, Keyword.valueList(), errorConsumer));

    var statements = functions(module).get(0).getBlock().statements();
    int i = 0;
    assertEquals(new AstBoolLiteral(true), ((AstIf) statements.get(i++)).getConditions().get(0));
    {
      var stmt = (AstDefer) statements.get(i++);
      var statements1 = stmt.getBlock().statements();
      assertEquals(1, statements1.size());
      assertEquals(
          new AstNullLiteral(), ((AstExpressionStatement) statements1.get(0)).getExpression());
    }
    assertEquals(
        new AstBoolLiteral(false),
        ((AstVariableStatement) statements.get(i++)).getVariable().getExpression());
    {
      var stmt = (AstFor) statements.get(i++);
      assertEquals(new AstNullLiteral(), stmt.getInit().getExpression());
      assertEquals(new AstBoolLiteral(true), stmt.getCondition());
      assertEquals(new AstBoolLiteral(true), stmt.getIncrement());
    }
    assertEquals(new AstBoolLiteral(true), ((AstReturn) statements.get(i++)).getExpression());
  }

  @Test
  void testReservedNameFailures() {
    var code =
        """
        module if;
        var return int;
        const else int = 1;
        function true() {}
        type record int {}
        type union byte {}
        """;

    var errorConsumer = new InMemoryErrorConsumer();
    var module = parseCode(code, errorConsumer);
    var nameMap = new NameMap();
    module.accept(new PreprocessVisitor(nameMap, Keyword.valueList(), errorConsumer));

    assertEquals(
        ImmutableList.of(
            "Reserved word cannot be used as a name: if",
            "Reserved word cannot be used as a name: return",
            "Reserved word cannot be used as a name: else",
            "Reserved word cannot be used as a name: true",
            "Reserved type name cannot be used as a type name: int",
            "Reserved type name cannot be used as a type name: byte"),
        errorConsumer.getErrors());
  }

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

    var errorConsumer = new InMemoryErrorConsumer();
    var module = parseCode(code, errorConsumer);
    var nameMap = new NameMap();
    module.accept(new PreprocessVisitor(nameMap, Keyword.valueList(), errorConsumer));

    assertEquals(List.of(), errorConsumer.getErrors());
    assertSame(variables(module).get(0), nameMap.getVariable("x"));
    assertSame(variables(module).get(1), nameMap.getVariable("y"));
    assertSame(functions(module).get(0), nameMap.getFunction("f"));
    assertSame(functions(module).get(1), nameMap.getFunction("g"));
    assertSame(types(module).get(0), nameMap.getType("R"));
  }

  @Test
  void testDuplicateVariableFailure() {
    var code =
        """
        module test;

        var x int;
        const x int;
        """;

    var errorConsumer = new InMemoryErrorConsumer();
    var module = parseCode(code, errorConsumer);
    var nameMap = new NameMap();
    module.accept(new PreprocessVisitor(nameMap, Keyword.valueList(), errorConsumer));

    assertEquals(
        List.of("Duplicate variable: test::x. Defined at 3,5."), errorConsumer.getErrors());
  }

  @Test
  void testDuplicateFunctionFailure() {
    var code =
        """
        module test;

        function f() {}
        function f() {}
        """;

    var errorConsumer = new InMemoryErrorConsumer();
    var module = parseCode(code, errorConsumer);
    var nameMap = new NameMap();
    module.accept(new PreprocessVisitor(nameMap, Keyword.valueList(), errorConsumer));

    assertEquals(
        List.of("Duplicate function: test::f. Defined at 3,10."), errorConsumer.getErrors());
  }

  @Test
  void testDuplicateTypeFailure() {
    var code =
        """
        module test;

        type record R {}
        type record R {}
        """;

    var errorConsumer = new InMemoryErrorConsumer();
    var module = parseCode(code, errorConsumer);
    var nameMap = new NameMap();
    module.accept(new PreprocessVisitor(nameMap, Keyword.valueList(), errorConsumer));

    assertEquals(List.of("Duplicate type: test::R. Defined at 3,13."), errorConsumer.getErrors());
  }
}
