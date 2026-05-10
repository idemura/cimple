package com.github.idemura.cimple.compiler.semantics;

import static com.github.idemura.cimple.compiler.ast.AstUtils.*;
import static com.github.idemura.cimple.compiler.parser.Parser.parseCode;
import static org.junit.jupiter.api.Assertions.*;

import com.github.idemura.cimple.compiler.ast.AstBuiltinType;
import com.github.idemura.cimple.compiler.ast.AstDefer;
import com.github.idemura.cimple.compiler.ast.AstExpressionStatement;
import com.github.idemura.cimple.compiler.ast.AstFor;
import com.github.idemura.cimple.compiler.ast.AstIf;
import com.github.idemura.cimple.compiler.ast.AstLocal;
import com.github.idemura.cimple.compiler.ast.AstRecordType;
import com.github.idemura.cimple.compiler.ast.AstReturn;
import com.github.idemura.cimple.compiler.ast.AstTypeRef;
import com.github.idemura.cimple.compiler.parser.Keyword;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.jupiter.api.Test;

class PreprocessVisitorTest extends AbstractSemanticsTest {
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
    var module = parseCode(code, errorConsumer);
    module.accept(new PreprocessVisitor(Keyword.valueList(), errorConsumer));
    var statements = module.findFunction("f").block().statements();
    int i = 0;
    assertEquals(boolLiteral(true), ((AstIf) statements.get(i++)).conditions().get(0));
    {
      var stmt = (AstDefer) statements.get(i++);
      var deferStatements = stmt.block().statements();
      assertEquals(1, deferStatements.size());
      assertEquals(nullLiteral(), ((AstExpressionStatement) deferStatements.get(0)).expression());
    }
    assertEquals(boolLiteral(false), ((AstLocal) statements.get(i++)).variable().expression());
    {
      var stmt = (AstFor) statements.get(i++);
      assertEquals(nullLiteral(), stmt.init().expression());
      assertEquals(boolLiteral(true), stmt.condition());
      assertEquals(boolLiteral(true), stmt.increment());
    }
    assertEquals(boolLiteral(true), ((AstReturn) statements.get(i++)).expression());
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
    var module = parseCode(code, errorConsumer);
    module.accept(new PreprocessVisitor(Keyword.valueList(), errorConsumer));
    assertEquals(
        ImmutableList.of(
            "Reserved word 'if' cannot be used as a name",
            "Reserved word 'return' cannot be used as a name",
            "Reserved word 'else' cannot be used as a name",
            "Reserved word 'true' cannot be used as a name",
            "Reserved word 'int' cannot be used as a type name",
            "Reserved word 'byte' cannot be used as a type name"),
        errorConsumer.errors());
  }

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
    module.accept(new PreprocessVisitor(Keyword.valueList(), errorConsumer));
    assertEquals(List.of(), errorConsumer.errors());
    {
      var header = module.findReceiverFunction("Duration", "toMillis").header();
      var receiverType = AstTypeRef.ofName("Duration");
      assertEquals(receiverType, header.receiverType());
      assertEquals(1, header.receiverIndex());
      assertEquals(AstTypeRef.ofType(AstBuiltinType.INT64), header.parameters().get(0).typeRef());
      assertEquals(receiverType, header.parameters().get(1).typeRef());
      assertEquals(AstTypeRef.ofType(AstBuiltinType.VOID), header.resultType());
    }
    {
      var header = module.findFunction("f").header();
      assertEquals(-1, header.receiverIndex());
      assertEquals(AstTypeRef.ofType(AstBuiltinType.VOID), header.resultType());
    }
  }

  @Test
  void testNormalizeIntAndFloatTypeRefs() {
    var code =
        """
        module test;
        type record R {
          var y float;
        }
        var x int;
        function f(a int) float {}
        """;
    var module = parseCode(code, errorConsumer);
    module.accept(new PreprocessVisitor(Keyword.valueList(), errorConsumer));
    assertEquals(List.of(), errorConsumer.errors());
    assertEquals(AstTypeRef.ofType(AstBuiltinType.INT64), module.findVariable("x").typeRef());
    assertEquals(
        AstTypeRef.ofType(AstBuiltinType.FLOAT64),
        ((AstRecordType) module.findType("R")).fields().get(0).typeRef());
    assertEquals(
        AstTypeRef.ofType(AstBuiltinType.INT64),
        module.findFunction("f").header().parameters().get(0).typeRef());
    assertEquals(
        AstTypeRef.ofType(AstBuiltinType.FLOAT64), module.findFunction("f").header().resultType());
  }

  @Test
  void testNormalizeAliases() {
    var code =
        """
        module test;
        type record R {
          var f float;
        }
        var g int;
        function f(p int) {
          var l float;
        }
        """;
    var module = parseCode(code, errorConsumer);
    module.accept(new PreprocessVisitor(Keyword.valueList(), errorConsumer));
    assertEquals(List.of(), errorConsumer.errors());
    assertEquals(AstTypeRef.ofType(AstBuiltinType.INT64), module.findVariable("g").typeRef());
    assertEquals(
        AstTypeRef.ofType(AstBuiltinType.FLOAT64),
        ((AstRecordType) module.findType("R")).fields().get(0).typeRef());
    assertEquals(
        AstTypeRef.ofType(AstBuiltinType.INT64),
        module.findFunction("f").header().parameters().get(0).typeRef());
    assertEquals(
        AstTypeRef.ofType(AstBuiltinType.FLOAT64),
        ((AstLocal) module.findFunction("f").block().statements().get(0)).variable().typeRef());
  }

  @Test
  void testReceiverFunctionMustHaveExactlyOneUntypedParameter() {
    var code =
        """
        module test;
        type record Duration {}
        function Duration:a(x int) {}
        function Duration:b(x, y) {}
        """;
    var module = parseCode(code, errorConsumer);
    module.accept(new PreprocessVisitor(Keyword.valueList(), errorConsumer));
    assertEquals(
        List.of(
            "Receiver function 'Duration:a': missing the receiver parameter",
            "Receiver function 'Duration:b': multiple receiver parameters"),
        errorConsumer.errors());
  }

  @Test
  void testFreeFunctionCannotHaveUntypedParameter() {
    var code =
        """
        module test;
        function f(x) {}
        """;
    var module = parseCode(code, errorConsumer);
    module.accept(new PreprocessVisitor(Keyword.valueList(), errorConsumer));
    assertEquals(
        List.of("Free function 'f' cannot have a receiver parameter 'x'"), errorConsumer.errors());
  }

  @Test
  void testVariableMustHaveTypeOrInitializer() {
    var code =
        """
        module test;
        var x;
        type record R {
          var y;
        }
        function f() {
          const z;
        }
        """;
    var module = parseCode(code, errorConsumer);
    module.accept(new PreprocessVisitor(Keyword.valueList(), errorConsumer));
    assertEquals(
        List.of(
            "Variable 'x' must have a type or an initializer",
            "Variable 'y' must have a type or an initializer",
            "Variable 'z' must have a type or an initializer"),
        errorConsumer.errors());
  }

  @Test
  void testDuplicateRecordFieldFailure() {
    var code =
        """
        module test;
        type record R {
          var x int;
          const x int;
        }
        """;
    var module = parseCode(code, errorConsumer);
    module.accept(new PreprocessVisitor(Keyword.valueList(), errorConsumer));
    assertEquals(
        List.of("Duplicate record field 'x'. First defined at 3,7."), errorConsumer.errors());
  }

  @Test
  void testDuplicateUnionVariantFailure() {
    var code =
        """
        module test;
        type union U {
          A;
          A(int);
        }
        """;
    var module = parseCode(code, errorConsumer);
    module.accept(new PreprocessVisitor(Keyword.valueList(), errorConsumer));
    assertEquals(
        List.of("Duplicate union variant 'A'. First defined at 3,3."), errorConsumer.errors());
  }
}
