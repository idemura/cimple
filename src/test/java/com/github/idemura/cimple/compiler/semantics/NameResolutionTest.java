package com.github.idemura.cimple.compiler.semantics;

import static com.github.idemura.cimple.compiler.parser.Parser.parseCode;
import static org.junit.jupiter.api.Assertions.*;

import com.github.idemura.cimple.compiler.InMemoryErrorConsumer;
import com.github.idemura.cimple.compiler.QualifiedName;
import com.github.idemura.cimple.compiler.ast.AstBuiltinType;
import com.github.idemura.cimple.compiler.ast.AstTypeRef;
import java.util.List;
import org.junit.jupiter.api.Test;

class NameResolutionTest {
  private final InMemoryErrorConsumer errorConsumer = new InMemoryErrorConsumer();

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

    var module = parseCode(code, errorConsumer);
    var semanticAnalyzer = new SemanticAnalyzer(errorConsumer);
    assertTrue(semanticAnalyzer.analyze(module));

    assertEquals(List.of(), errorConsumer.errors());
    var nameMap = semanticAnalyzer.nameMap();
    assertSame(module.findVariable("x"), nameMap.lookupEntity(new QualifiedName("x")));
    assertSame(module.findVariable("y"), nameMap.lookupEntity(new QualifiedName("y")));
    assertSame(module.findFunction("f"), nameMap.lookupEntity(new QualifiedName("f")));
    assertSame(module.findFunction("g"), nameMap.lookupEntity(new QualifiedName("g")));
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

    var module = parseCode(code, errorConsumer);
    var semanticAnalyzer = new SemanticAnalyzer(errorConsumer);
    assertTrue(semanticAnalyzer.analyze(module));

    assertEquals(List.of(), errorConsumer.errors());
    {
      var header = module.findReceiverFunction("Duration", "toMillis").header();
      var receiverType = AstTypeRef.ofName("test", "Duration");
      assertEquals(receiverType, header.receiverType());
      assertEquals(1, header.receiverIndex());
      assertEquals(receiverType, header.parameters().get(1).typeRef());
      assertEquals(AstTypeRef.ofType(AstBuiltinType.VOID), header.resultType());
    }
    {
      var header = module.findFunction("f").header();
      assertEquals(-1, header.receiverIndex());
      assertEquals(AstTypeRef.ofType(AstBuiltinType.VOID), header.resultType());
    }
    var nameMap = semanticAnalyzer.nameMap();
    assertSame(
        module.findReceiverFunction("Duration", "toMillis"),
        nameMap.lookupReceiverFunction(new QualifiedName("test", "Duration"), "toMillis"));
    assertNull(nameMap.lookupEntity(new QualifiedName("toMillis")));
  }

  @Test
  void testDuplicateVariableFailure() {
    var code =
        """
        module test;
        var x int;
        const x int;
        """;
    var module = parseCode(code, errorConsumer);
    var semanticAnalyzer = new SemanticAnalyzer(errorConsumer);
    assertFalse(semanticAnalyzer.analyze(module));
    assertEquals(
        List.of("Definition of variable 'x' has a name collision with variable defined at 2,5"),
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
    var module = parseCode(code, errorConsumer);
    var semanticAnalyzer = new SemanticAnalyzer(errorConsumer);
    assertFalse(semanticAnalyzer.analyze(module));
    assertEquals(
        List.of("Definition of function 'f' has a name collision with function defined at 2,10"),
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
    var module = parseCode(code, errorConsumer);
    var semanticAnalyzer = new SemanticAnalyzer(errorConsumer);
    assertFalse(semanticAnalyzer.analyze(module));
    assertEquals(
        List.of("Definition of function 'f' has a name collision with variable defined at 2,5"),
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
    var module = parseCode(code, errorConsumer);
    var semanticAnalyzer = new SemanticAnalyzer(errorConsumer);
    assertFalse(semanticAnalyzer.analyze(module));
    assertEquals(List.of("Duplicate type: 'test~R'. Defined at 2,13."), errorConsumer.errors());
  }
}
