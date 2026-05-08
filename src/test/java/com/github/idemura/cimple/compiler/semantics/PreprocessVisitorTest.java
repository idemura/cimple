package com.github.idemura.cimple.compiler.semantics;

import static com.github.idemura.cimple.compiler.parser.Parser.parseCode;
import static org.junit.jupiter.api.Assertions.*;

import com.github.idemura.cimple.compiler.InMemoryErrorConsumer;
import com.github.idemura.cimple.compiler.QualifiedName;
import com.github.idemura.cimple.compiler.ast.AstBoolLiteral;
import com.github.idemura.cimple.compiler.ast.AstBuiltinType;
import com.github.idemura.cimple.compiler.ast.AstDefer;
import com.github.idemura.cimple.compiler.ast.AstExpressionStatement;
import com.github.idemura.cimple.compiler.ast.AstFor;
import com.github.idemura.cimple.compiler.ast.AstIf;
import com.github.idemura.cimple.compiler.ast.AstLocal;
import com.github.idemura.cimple.compiler.ast.AstNullLiteral;
import com.github.idemura.cimple.compiler.ast.AstRecordType;
import com.github.idemura.cimple.compiler.ast.AstReturn;
import com.github.idemura.cimple.compiler.ast.AstTypeRef;
import com.github.idemura.cimple.compiler.parser.Keyword;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.jupiter.api.Test;

class PreprocessVisitorTest {
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

    var statements = module.findFunction("f").block().statements();
    int i = 0;
    assertEquals(new AstBoolLiteral(true), ((AstIf) statements.get(i++)).conditions().get(0));
    {
      var stmt = (AstDefer) statements.get(i++);
      var statements1 = stmt.block().statements();
      assertEquals(1, statements1.size());
      assertEquals(
          new AstNullLiteral(), ((AstExpressionStatement) statements1.get(0)).expression());
    }
    assertEquals(
        new AstBoolLiteral(false), ((AstLocal) statements.get(i++)).variable().expression());
    {
      var stmt = (AstFor) statements.get(i++);
      assertEquals(new AstNullLiteral(), stmt.init().expression());
      assertEquals(new AstBoolLiteral(true), stmt.condition());
      assertEquals(new AstBoolLiteral(true), stmt.increment());
    }
    assertEquals(new AstBoolLiteral(true), ((AstReturn) statements.get(i++)).expression());
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
        errorConsumer.errors());
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

    assertEquals(List.of(), errorConsumer.errors());
    assertSame(module.findVariable("x"), nameMap.lookupEntity("x"));
    assertSame(module.findVariable("y"), nameMap.lookupEntity("y"));
    assertSame(module.findFunction("f"), nameMap.lookupEntity("f"));
    assertSame(module.findFunction("g"), nameMap.lookupEntity("g"));
    assertSame(module.findType("R"), nameMap.lookupType(new QualifiedName("R")));
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

    var errorConsumer = new InMemoryErrorConsumer();
    var module = parseCode(code, errorConsumer);
    var nameMap = new NameMap();
    module.accept(new PreprocessVisitor(nameMap, Keyword.valueList(), errorConsumer));

    assertEquals(List.of(), errorConsumer.errors());
    {
      var header = module.findReceiverFunction("Duration", "toMillis").header();
      var receiverType = AstTypeRef.ofName("test", "Duration");
      assertEquals(receiverType, header.receiverType());
      assertEquals(1, header.receiverIndex());
      assertEquals(receiverType, header.parameters().get(1).type());
      assertEquals(AstTypeRef.ofType(AstBuiltinType.VOID), header.resultType());
    }
    {
      var header = module.findFunction("f").header();
      assertEquals(-1, header.receiverIndex());
      assertEquals(AstTypeRef.ofType(AstBuiltinType.VOID), header.resultType());
    }
    assertSame(
        module.findReceiverFunction("Duration", "toMillis"),
        nameMap.lookupReceiverFunction(new QualifiedName("test", "Duration"), "toMillis"));
    assertNull(nameMap.lookupEntity("toMillis"));
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

    var errorConsumer = new InMemoryErrorConsumer();
    var module = parseCode(code, errorConsumer);
    var nameMap = new NameMap();
    module.accept(new PreprocessVisitor(nameMap, Keyword.valueList(), errorConsumer));

    assertEquals(List.of(), errorConsumer.errors());
    assertEquals(AstTypeRef.ofType(AstBuiltinType.INT64), module.findVariable("x").type());
    assertEquals(
        AstTypeRef.ofType(AstBuiltinType.FLOAT64),
        ((AstRecordType) module.findType("R")).fields().get(0).type());
    assertEquals(
        AstTypeRef.ofType(AstBuiltinType.INT64),
        module.findFunction("f").header().parameters().get(0).type());
    assertEquals(
        AstTypeRef.ofType(AstBuiltinType.FLOAT64), module.findFunction("f").header().resultType());
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

    var errorConsumer = new InMemoryErrorConsumer();
    var module = parseCode(code, errorConsumer);
    var nameMap = new NameMap();
    module.accept(new PreprocessVisitor(nameMap, Keyword.valueList(), errorConsumer));

    assertEquals(
        List.of(
            "Receiver function test::a: missing the receiver parameter.",
            "Receiver function test::b: multiple receiver parameters."),
        errorConsumer.errors());
  }

  @Test
  void testFreeFunctionCannotHaveUntypedParameter() {
    var code =
        """
        module test;

        function f(x) {}
        """;

    var errorConsumer = new InMemoryErrorConsumer();
    var module = parseCode(code, errorConsumer);
    var nameMap = new NameMap();
    module.accept(new PreprocessVisitor(nameMap, Keyword.valueList(), errorConsumer));

    assertEquals(
        List.of("Free function test::f cannot have a receiver parameter x."),
        errorConsumer.errors());
  }

  @Test
  void testVariableMustHaveTypeOrInitializer() {
    var code =
        """
        module test;

        var x;

        type record R {
          var z;
        }

        function f() {
          const y;
        }
        """;

    var errorConsumer = new InMemoryErrorConsumer();
    var module = parseCode(code, errorConsumer);
    var nameMap = new NameMap();
    module.accept(new PreprocessVisitor(nameMap, Keyword.valueList(), errorConsumer));

    assertEquals(
        List.of(
            "Variable test::x must have a type or an initializer.",
            "Variable z must have a type or an initializer.",
            "Variable y must have a type or an initializer."),
        errorConsumer.errors());
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
        List.of(
            "Definition of variable test::x has a name collision with variable defined at 3,5."),
        errorConsumer.errors());
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
        List.of(
            "Definition of function test::f has a name collision with function defined at 3,10."),
        errorConsumer.errors());
  }

  @Test
  void testFunctionVariableCollisionFailure() {
    var code =
        """
        module test;

        var f int;
        function f() {}
        """;

    var errorConsumer = new InMemoryErrorConsumer();
    var module = parseCode(code, errorConsumer);
    var nameMap = new NameMap();
    module.accept(new PreprocessVisitor(nameMap, Keyword.valueList(), errorConsumer));

    assertEquals(
        List.of(
            "Definition of function test::f has a name collision with variable defined at 3,5."),
        errorConsumer.errors());
  }

  @Test
  void testVariableFunctionCollisionFailure() {
    var code =
        """
        module test;

        function f() {}
        var f int;
        """;

    var errorConsumer = new InMemoryErrorConsumer();
    var module = parseCode(code, errorConsumer);
    var nameMap = new NameMap();
    module.accept(new PreprocessVisitor(nameMap, Keyword.valueList(), errorConsumer));

    assertEquals(
        List.of(
            "Definition of variable test::f has a name collision with function defined at 3,10."),
        errorConsumer.errors());
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

    assertEquals(List.of("Duplicate type: test::R. Defined at 3,13."), errorConsumer.errors());
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

    var errorConsumer = new InMemoryErrorConsumer();
    var module = parseCode(code, errorConsumer);
    var nameMap = new NameMap();
    module.accept(new PreprocessVisitor(nameMap, Keyword.valueList(), errorConsumer));

    assertEquals(List.of("Duplicate record field: x. Defined at 4,7."), errorConsumer.errors());
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

    var errorConsumer = new InMemoryErrorConsumer();
    var module = parseCode(code, errorConsumer);
    var nameMap = new NameMap();
    module.accept(new PreprocessVisitor(nameMap, Keyword.valueList(), errorConsumer));

    assertEquals(List.of("Duplicate union variant: A. Defined at 4,3."), errorConsumer.errors());
  }
}
