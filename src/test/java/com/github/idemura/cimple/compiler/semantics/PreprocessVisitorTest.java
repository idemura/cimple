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
    var module = parseCode(code);
    module.accept(new PreprocessVisitor(Keyword.valueList(), errorConsumer));
    var statements = module.findFunction("f").block().statements();
    int i = 0;
    assertEquals(boolLiteral(true), ((AstIf) statements.get(i++)).conditions().get(0).root());
    {
      var stmt = (AstDefer) statements.get(i++);
      var deferStatements = stmt.block().statements();
      assertEquals(1, deferStatements.size());
      assertEquals(
          nullLiteral(), ((AstExpressionStatement) deferStatements.get(0)).expression().root());
    }
    assertEquals(
        boolLiteral(false), ((AstLocal) statements.get(i++)).variable().expression().root());
    {
      var stmt = (AstFor) statements.get(i++);
      assertEquals(nullLiteral(), stmt.init().expression().root());
      assertEquals(boolLiteral(true), stmt.condition().root());
      assertEquals(boolLiteral(true), stmt.increment().root());
    }
    assertEquals(boolLiteral(true), ((AstReturn) statements.get(i++)).expression().root());
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
    var module = parseCode(code);
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
    var module = parseCode(code);
    module.accept(new PreprocessVisitor(Keyword.valueList(), errorConsumer));
    assertEquals(List.of(), errorConsumer.errors());
    {
      var header = module.findReceiverFunction("Duration", "toMillis").header();
      var receiverType = newTypeRef("Duration");
      assertEquals(receiverType, header.receiverType());
      assertEquals(1, header.receiverIndex());
      assertEquals(newBuiltinTypeRef("int64"), header.parameters().get(0).type());
      assertEquals(receiverType, header.parameters().get(1).type());
      assertEquals(AstBuiltinType.VOID, header.resultType());
    }
    {
      var header = module.findFunction("f").header();
      assertEquals(-1, header.receiverIndex());
      assertEquals(AstBuiltinType.VOID, header.resultType());
    }
  }

  @Test
  void testNormalizeTypeAliases() {
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
    var module = parseCode(code);
    module.accept(new PreprocessVisitor(Keyword.valueList(), errorConsumer));
    assertEquals(List.of(), errorConsumer.errors());
    assertEquals(newBuiltinTypeRef("int64"), module.findVariable("g").type());
    assertEquals(
        newBuiltinTypeRef("float64"),
        ((AstRecordType) module.findType("R")).fields().get(0).type());
    assertEquals(
        newBuiltinTypeRef("int64"), module.findFunction("f").header().parameters().get(0).type());
    assertEquals(
        newBuiltinTypeRef("float64"),
        ((AstLocal) module.findFunction("f").block().statements().get(0)).variable().type());
  }

  @Test
  void testFunctionParameterErrors() {
    var code =
        """
        module test;
        type record Duration {}
        function Duration:a(x bool) {}
        function Duration:b(x, y) {}
        function f(x) {}
        """;
    var module = parseCode(code);
    module.accept(new PreprocessVisitor(Keyword.valueList(), errorConsumer));
    assertEquals(
        List.of(
            "Receiver function 'Duration:a': missing the receiver parameter",
            "Receiver function 'Duration:b': multiple receiver parameters",
            "Free function 'f' cannot have a receiver parameter 'x'"),
        errorConsumer.errors());
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
    var module = parseCode(code);
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
    var module = parseCode(code);
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
    var module = parseCode(code);
    module.accept(new PreprocessVisitor(Keyword.valueList(), errorConsumer));
    assertEquals(
        List.of("Duplicate union variant 'A'. First defined at 3,3."), errorConsumer.errors());
  }
}
