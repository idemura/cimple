package io.lang.cimple.compiler;

import static io.lang.cimple.compiler.AstAssertionUtils.*;
import static io.lang.cimple.compiler.AstTreeUtils.*;
import static org.junit.jupiter.api.Assertions.*;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ParserTest extends AbstractTest {
  @Test
  void testVariableSyntax() {
    var code =
        """
        module test;
        var typedAndInitialized int = 1;
        var typed int;
        var initialized = 2;
        var bare;
        function f() {
          var localTypedAndInitialized int = 3;
          var localTyped int;
          var localInitialized = 4;
          var localBare;
        }
        """;
    var module = parseCode(code);
    assertVariableSyntax(
        module.findVariable("typedAndInitialized"), "typedAndInitialized", "int", 1L);
    assertVariableSyntax(module.findVariable("typed"), "typed", "int", null);
    assertVariableSyntax(module.findVariable("initialized"), "initialized", null, 2L);
    assertVariableSyntax(module.findVariable("bare"), "bare", null, null);

    var statements = module.findFunction("f").block().statements();
    assertEquals(4, statements.size());
    int i = 0;
    {
      var variable = ((AstLocal) statements.get(i++)).variable();
      assertVariableSyntax(variable, "localTypedAndInitialized", "int", 3L);
    }
    {
      var variable = ((AstLocal) statements.get(i++)).variable();
      assertVariableSyntax(variable, "localTyped", "int", null);
    }
    {
      var variable = ((AstLocal) statements.get(i++)).variable();
      assertVariableSyntax(variable, "localInitialized", null, 4L);
    }
    {
      var variable = ((AstLocal) statements.get(i++)).variable();
      assertVariableSyntax(variable, "localBare", null, null);
    }
  }

  @Test
  void testModule() {
    var code =
        """
        module test;
        var v0 int = 5;
        var v1 int;
        var v2 = 5;
        var p int*;
        var pp int**;
        var a int[];
        var ap int[]*;
        var pa int*[];
        const c0 int = 7;
        function f0() {}
        function f1(x int) {}
        function f2(x int, y int) {}
        function rv() int {
          return 1;
        }
        """;
    var module = parseCode(code);
    assertEquals(new Identifier("test"), module.name());
    {
      var f = module.findFunction("f0");
      assertEquals(new Identifier("f0"), f.name());
      assertNull(f.resultType());
      assertEquals(ImmutableList.of(), f.parameters());
    }
    {
      var f = module.findFunction("f1");
      assertEquals(new Identifier("f1"), f.name());
      assertNull(f.resultType());
      var params = f.parameters();
      assertEquals(1, params.size());
      assertEquals(rawVariable("x", "int"), params.get(0));
    }
    {
      var f = module.findFunction("f2");
      assertEquals(new Identifier("f2"), f.name());
      assertNull(f.resultType());
      var params = f.parameters();
      assertEquals(2, params.size());
      assertEquals(rawVariable("x", "int"), params.get(0));
      assertEquals(rawVariable("y", "int"), params.get(1));
    }
    {
      var f = module.findFunction("rv");
      assertEquals(new Identifier("rv"), f.name());
      assertEquals(newTypeRef("int"), f.resultType());
      assertEquals(ImmutableList.of(), f.parameters());
    }
    {
      var v = module.findVariable("v0");
      assertEquals(new Identifier("v0"), v.name());
      assertEquals(newTypeRef("int"), v.type());
      assertTrue(v.getBit(AstVariable.MUTABLE));
    }
    {
      var v = module.findVariable("v1");
      assertEquals(new Identifier("v1"), v.name());
      assertEquals(newTypeRef("int"), v.type());
      assertTrue(v.getBit(AstVariable.MUTABLE));
    }
    {
      var v = module.findVariable("v2");
      assertEquals(new Identifier("v2"), v.name());
      assertNull(v.type());
      assertTrue(v.getBit(AstVariable.MUTABLE));
    }
    {
      var v = module.findVariable("p");
      assertEquals(new Identifier("p"), v.name());
      assertEquals(new AstPointerType(newTypeRef("int")), v.type());
      assertTrue(v.getBit(AstVariable.MUTABLE));
    }
    {
      var v = module.findVariable("pp");
      assertEquals(new Identifier("pp"), v.name());
      assertEquals(new AstPointerType(new AstPointerType(newTypeRef("int"))), v.type());
      assertTrue(v.getBit(AstVariable.MUTABLE));
    }
    {
      var v = module.findVariable("a");
      assertEquals(new Identifier("a"), v.name());
      assertEquals(new AstArrayType(newTypeRef("int")), v.type());
      assertTrue(v.getBit(AstVariable.MUTABLE));
    }
    {
      var v = module.findVariable("ap");
      assertEquals(new Identifier("ap"), v.name());
      assertEquals(new AstPointerType(new AstArrayType(newTypeRef("int"))), v.type());
      assertTrue(v.getBit(AstVariable.MUTABLE));
    }
    {
      var v = module.findVariable("pa");
      assertEquals(new Identifier("pa"), v.name());
      assertEquals(new AstArrayType(new AstPointerType(newTypeRef("int"))), v.type());
      assertTrue(v.getBit(AstVariable.MUTABLE));
    }
    {
      var v = module.findVariable("c0");
      assertEquals(new Identifier("c0"), v.name());
      assertEquals(newTypeRef("int"), v.type());
      assertFalse(v.getBit(AstVariable.MUTABLE));
    }
  }

  @Test
  void testFunctionDeclaration() {
    var code =
        """
        module test;
        function external(x int) string;
        """;
    var module = parseCode(code);
    {
      var f = module.findFunction("external");
      assertEquals(new Identifier("external"), f.name());
      assertEquals(newTypeRef("string"), f.resultType());
      assertEquals(ImmutableList.of(rawVariable("x", "int")), f.parameters());
      assertNull(f.block());
    }
  }

  @Test
  void testGenericFunctionSyntax() {
    var code =
        """
        module test;
        generic (T) function size(a T[]);
        generic (T) function first(a T[]) T {
          return a[0];
        }
        """;
    var module = parseCode(code);
    {
      var function = module.findFunction("size");
      assertEquals(ImmutableList.of(wildcard("T")), function.wildcards());
      assertEquals(
          ImmutableList.of(
              new AstVariable(new Identifier("a"), new AstArrayType(newTypeRef("T")), null)),
          function.parameters());
      assertNull(function.block());
    }
    {
      var function = module.findFunction("first");
      assertEquals(ImmutableList.of(wildcard("T")), function.wildcards());
      assertEquals(newTypeRef("T"), function.resultType());
      assertNotNull(function.block());
    }
  }

  @Test
  void testGenericFunctionSyntaxFailures() {
    {
      var code =
          """
          module test;
          generic () function f();
          """;
      assertThrows(CompilerException.class, () -> parseCode(code));
    }
    {
      var code =
          """
          module test;
          generic (T,) function f();
          """;
      assertThrows(CompilerException.class, () -> parseCode(code));
    }
  }

  @Test
  void testStructType() {
    var code =
        """
        module test;
        type struct Empty {}
        type struct Point {
          var x int;
          var y int;
          const name string;
        }
        """;
    var module = parseCode(code);
    assertEquals(new Identifier("test"), module.name());
    {
      var type = (AstStructType) module.findType("Empty");
      assertEquals(new Identifier("Empty"), type.name());
      assertEquals(ImmutableList.of(), type.fields());
    }
    {
      var type = (AstStructType) module.findType("Point");
      assertEquals(new Identifier("Point"), type.name());
      var fields = type.fields();
      assertEquals(3, fields.size());
      int j = 0;
      {
        var f = fields.get(j++);
        assertEquals(new Identifier("x"), f.name());
        assertEquals(newTypeRef("int"), f.type());
        assertTrue(f.getBit(AstVariable.MUTABLE));
      }
      {
        var f = fields.get(j++);
        assertEquals(new Identifier("y"), f.name());
        assertEquals(newTypeRef("int"), f.type());
        assertTrue(f.getBit(AstVariable.MUTABLE));
      }
      {
        var f = fields.get(j++);
        assertEquals(new Identifier("name"), f.name());
        assertEquals(newTypeRef("string"), f.type());
        assertFalse(f.getBit(AstVariable.MUTABLE));
      }
    }
  }

  @Test
  void testUnionType() {
    var code =
        """
        module test;
        type union Maybe {
          None;
          Some(string);
        }
        """;
    var module = parseCode(code);
    assertEquals(new Identifier("test"), module.name());
    var type = (AstUnionType) module.findType("Maybe");
    assertEquals(new Identifier("Maybe"), type.name());
    assertEquals(
        ImmutableList.of(unionVariant("None", null), unionVariant("Some", "string")),
        type.variants());
  }

  @Test
  void testEnumType() {
    var code =
        """
        module test;
        type enum Color(int32) {
          Red;
          Green(3);
          Blue;
        }
        """;
    var module = parseCode(code);
    assertEquals(new Identifier("test"), module.name());
    var type = (AstEnumType) module.findType("Color");
    assertEquals(new Identifier("Color"), type.name());
    assertEquals(newTypeRef("int32"), type.baseType());
    var variants = type.variants();
    assertEquals(3, variants.size());
    int i = 0;
    {
      var variant = variants.get(i++);
      assertEnumVariant(variant, "Red", null);
    }
    {
      var variant = variants.get(i++);
      assertEnumVariant(variant, "Green", "3");
    }
    {
      var variant = variants.get(i++);
      assertEnumVariant(variant, "Blue", null);
    }
  }

  @Test
  void testFunctionType() {
    var code =
        """
        module test;
        type function Compare(a int, b int) bool;
        type function Supplier() string;
        type function Consumer(v string);
        """;
    var module = parseCode(code);
    assertEquals(new Identifier("test"), module.name());
    {
      var type = (AstFunctionType) module.findType("Compare");
      assertEquals(new Identifier("Compare"), type.name());
      assertEquals(newTypeRef("bool"), type.function().resultType());
      var params = type.function().parameters();
      assertEquals(2, params.size());
      assertEquals(rawVariable("a", "int"), params.get(0));
      assertEquals(rawVariable("b", "int"), params.get(1));
    }
    {
      var type = (AstFunctionType) module.findType("Supplier");
      assertEquals(new Identifier("Supplier"), type.name());
      assertEquals(newTypeRef("string"), type.function().resultType());
      assertEquals(ImmutableList.of(), type.function().parameters());
    }
    {
      var type = (AstFunctionType) module.findType("Consumer");
      assertEquals(new Identifier("Consumer"), type.name());
      assertNull(type.function().resultType());
      assertEquals(ImmutableList.of(rawVariable("v", "string")), type.function().parameters());
    }
  }

  @Test
  void testInterfaceType() {
    var code =
        """
        module test;
        type interface Reader {
          function read(buffer string) int;
          function close();
        }
        """;
    var module = parseCode(code);
    var type = (AstInterfaceType) module.findType("Reader");
    assertEquals(new Identifier("Reader"), type.name());
    var functions = type.functions();
    assertEquals(2, functions.size());
    {
      var function = functions.get(0);
      assertEquals(new Identifier("read"), function.name());
      assertNull(function.block());
      assertEquals(newTypeRef("int"), function.resultType());
      assertEquals(ImmutableList.of(rawVariable("buffer", "string")), function.parameters());
    }
    {
      var function = functions.get(1);
      assertEquals(new Identifier("close"), function.name());
      assertNull(function.block());
      assertNull(function.resultType());
      assertEquals(ImmutableList.of(), function.parameters());
    }
  }

  @Test
  void testExpressions() {
    var code =
        """
        module test;
        function f() {
          var x = 1;
          var x = 1 + 2;
          var x = 1 + 2 - 3;
          var x = 1 * 2;
          var x = 1 / 2;
          var x = 1 % 2;
          var x = 1 * 2 * 3;
          var x = 1 + 2 * 3;
          var x = 1 * 2 + 3;
          var x = (1 + 2);
          var x = (1 + 2 type int);
          var x = p.t * 5;
        }
        """;
    var module = parseCode(code);
    var statements = module.findFunction("f").block().statements();
    assertEquals(12, statements.size());
    int i = 0;
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression();
      assertEquals(AstNumberLiteral.of(1), expr);
    }
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression();
      {
        var call = (AstCall) expr;
        assertEquals(newBuiltinFunctionRef("+"), call.function());
        assertEquals(2, call.arguments().size());
        assertEquals(AstNumberLiteral.of(1), call.arguments().get(0));
        assertEquals(AstNumberLiteral.of(2), call.arguments().get(1));
      }
    }
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression();
      {
        var callSub = (AstCall) expr;
        assertEquals(newBuiltinFunctionRef("-"), callSub.function());
        assertEquals(2, callSub.arguments().size());
        {
          var callAdd = (AstCall) callSub.arguments().get(0);
          assertEquals(newBuiltinFunctionRef("+"), callAdd.function());
          assertEquals(2, callAdd.arguments().size());
          assertEquals(AstNumberLiteral.of(1), callAdd.arguments().get(0));
          assertEquals(AstNumberLiteral.of(2), callAdd.arguments().get(1));
        }
        assertEquals(AstNumberLiteral.of(3), callSub.arguments().get(1));
      }
    }
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression();
      {
        var callMul = (AstCall) expr;
        assertEquals(newBuiltinFunctionRef("*"), callMul.function());
        assertEquals(2, callMul.arguments().size());
        assertEquals(AstNumberLiteral.of(1), callMul.arguments().get(0));
        assertEquals(AstNumberLiteral.of(2), callMul.arguments().get(1));
      }
    }
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression();
      {
        var callDiv = (AstCall) expr;
        assertEquals(newBuiltinFunctionRef("/"), callDiv.function());
        assertEquals(2, callDiv.arguments().size());
        assertEquals(AstNumberLiteral.of(1), callDiv.arguments().get(0));
        assertEquals(AstNumberLiteral.of(2), callDiv.arguments().get(1));
      }
    }
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression();
      {
        var callMod = (AstCall) expr;
        assertEquals(newBuiltinFunctionRef("%"), callMod.function());
        assertEquals(2, callMod.arguments().size());
        assertEquals(AstNumberLiteral.of(1), callMod.arguments().get(0));
        assertEquals(AstNumberLiteral.of(2), callMod.arguments().get(1));
      }
    }
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression();
      {
        var callMul = (AstCall) expr;
        assertEquals(newBuiltinFunctionRef("*"), callMul.function());
        assertEquals(2, callMul.arguments().size());
        {
          var nestedCallMul = (AstCall) callMul.arguments().get(0);
          assertEquals(newBuiltinFunctionRef("*"), nestedCallMul.function());
          assertEquals(2, nestedCallMul.arguments().size());
          assertEquals(AstNumberLiteral.of(1), nestedCallMul.arguments().get(0));
          assertEquals(AstNumberLiteral.of(2), nestedCallMul.arguments().get(1));
        }
        assertEquals(AstNumberLiteral.of(3), callMul.arguments().get(1));
      }
    }
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression();
      {
        var callAdd = (AstCall) expr;
        assertEquals(newBuiltinFunctionRef("+"), callAdd.function());
        assertEquals(2, callAdd.arguments().size());
        assertEquals(AstNumberLiteral.of(1), callAdd.arguments().get(0));
        {
          var callMul = (AstCall) callAdd.arguments().get(1);
          assertEquals(newBuiltinFunctionRef("*"), callMul.function());
          assertEquals(2, callMul.arguments().size());
          assertEquals(AstNumberLiteral.of(2), callMul.arguments().get(0));
          assertEquals(AstNumberLiteral.of(3), callMul.arguments().get(1));
        }
      }
    }
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression();
      {
        var callAdd = (AstCall) expr;
        assertEquals(newBuiltinFunctionRef("+"), callAdd.function());
        assertEquals(2, callAdd.arguments().size());
        {
          var callMul = (AstCall) callAdd.arguments().get(0);
          assertEquals(newBuiltinFunctionRef("*"), callMul.function());
          assertEquals(2, callMul.arguments().size());
          assertEquals(AstNumberLiteral.of(1), callMul.arguments().get(0));
          assertEquals(AstNumberLiteral.of(2), callMul.arguments().get(1));
        }
        assertEquals(AstNumberLiteral.of(3), callAdd.arguments().get(1));
      }
    }
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression();
      {
        var call = (AstCall) expr;
        assertEquals(newBuiltinFunctionRef("+"), call.function());
        assertEquals(2, call.arguments().size());
        assertEquals(AstNumberLiteral.of(1), call.arguments().get(0));
        assertEquals(AstNumberLiteral.of(2), call.arguments().get(1));
      }
    }
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression();
      {
        var cast = (AstCast) expr;
        assertEquals(newTypeRef("int"), cast.type());
        var callAdd = (AstCall) cast.expression();
        assertEquals(newBuiltinFunctionRef("+"), callAdd.function());
        assertEquals(2, callAdd.arguments().size());
        assertEquals(AstNumberLiteral.of(1), callAdd.arguments().get(0));
        assertEquals(AstNumberLiteral.of(2), callAdd.arguments().get(1));
      }
    }
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression();
      {
        var call = (AstCall) expr;
        assertEquals(newBuiltinFunctionRef("*"), call.function());
        {
          var fieldAccess = (AstFieldAccess) call.arguments().get(0);
          assertEquals(newVariableRef("p"), fieldAccess.object());
          assertEquals("t", fieldAccess.fieldName());
        }
        assertEquals(AstNumberLiteral.of(5), call.arguments().get(1));
      }
    }
  }

  @Test
  void testComparisonExpressionParsing() {
    var code =
        """
        module test;
        function f() {
          var x = 1 < 2;
          var x = 1 <= 2;
          var x = 1 > 2;
          var x = 1 >= 2;
          var x = 1 == 2;
          var x = 1 != 2;
        }
        """;
    var module = parseCode(code);
    var statements = module.findFunction("f").block().statements();
    assertEquals(6, statements.size());
    int i = 0;
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression();
      var call = (AstCall) expr;
      assertEquals(newBuiltinFunctionRef("<"), call.function());
      assertEquals(2, call.arguments().size());
      assertEquals(AstNumberLiteral.of(1), call.arguments().get(0));
      assertEquals(AstNumberLiteral.of(2), call.arguments().get(1));
    }
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression();
      var call = (AstCall) expr;
      assertEquals(newBuiltinFunctionRef("<="), call.function());
      assertEquals(2, call.arguments().size());
      assertEquals(AstNumberLiteral.of(1), call.arguments().get(0));
      assertEquals(AstNumberLiteral.of(2), call.arguments().get(1));
    }
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression();
      var call = (AstCall) expr;
      assertEquals(newBuiltinFunctionRef(">"), call.function());
      assertEquals(2, call.arguments().size());
      assertEquals(AstNumberLiteral.of(1), call.arguments().get(0));
      assertEquals(AstNumberLiteral.of(2), call.arguments().get(1));
    }
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression();
      var call = (AstCall) expr;
      assertEquals(newBuiltinFunctionRef(">="), call.function());
      assertEquals(2, call.arguments().size());
      assertEquals(AstNumberLiteral.of(1), call.arguments().get(0));
      assertEquals(AstNumberLiteral.of(2), call.arguments().get(1));
    }
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression();
      var call = (AstCall) expr;
      assertEquals(newBuiltinFunctionRef("=="), call.function());
      assertEquals(2, call.arguments().size());
      assertEquals(AstNumberLiteral.of(1), call.arguments().get(0));
      assertEquals(AstNumberLiteral.of(2), call.arguments().get(1));
    }
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression();
      var call = (AstCall) expr;
      assertEquals(newBuiltinFunctionRef("!="), call.function());
      assertEquals(2, call.arguments().size());
      assertEquals(AstNumberLiteral.of(1), call.arguments().get(0));
      assertEquals(AstNumberLiteral.of(2), call.arguments().get(1));
    }
  }

  @Test
  void testInvokeExpression() {
    // Parenthesized calls stay unambiguous because a cast requires `type` before `)`.
    var code =
        """
        module test;
        function f() {
          var x = foo~bar;
          var x = foo();
          var x = (foo)();
          var x = foo(1, 2);
          var x = foo~bar();
          var x = foo!();
          var x = (foo)!(1);
        }
        """;
    var module = parseCode(code);
    var statements = module.findFunction("f").block().statements();
    assertEquals(7, statements.size());
    int i = 0;
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression();
      var variableRef = (AstVariableRef) expr;
      assertEquals(newVariableRef("foo", "bar"), variableRef);
    }
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression();
      var call = (AstCall) expr;
      assertEquals(newFunctionRef("foo"), call.function());
      assertEquals(ImmutableList.of(), call.arguments());
    }
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression();
      var call = (AstCall) expr;
      assertEquals(newFunctionRef("foo"), call.function());
      assertEquals(ImmutableList.of(), call.arguments());
    }
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression();
      var call = (AstCall) expr;
      assertEquals(newFunctionRef("foo"), call.function());
      assertEquals(2, call.arguments().size());
      assertEquals(AstNumberLiteral.of(1), call.arguments().get(0));
      assertEquals(AstNumberLiteral.of(2), call.arguments().get(1));
    }
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression();
      var call = (AstCall) expr;
      assertEquals(newFunctionRef("foo", "bar"), call.function());
      assertEquals(ImmutableList.of(), call.arguments());
    }
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression();
      var call = (AstFunctionPointerCall) expr;
      assertEquals(newVariableRef("foo"), call.function());
      assertEquals(ImmutableList.of(), call.arguments());
    }
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression();
      var call = (AstFunctionPointerCall) expr;
      assertEquals(newVariableRef("foo"), call.function());
      assertEquals(ImmutableList.of(AstNumberLiteral.of(1)), call.arguments());
    }
  }

  @Test
  void testFieldArrayCallChain() {
    var code =
        """
        module test;
        function f() {
          var x = foo.bar;
          var x = foo.bar!();
          var x = foo[1];
          var x = foo.bar!(1, 2)[3].baz!();
        }
        """;
    var module = parseCode(code);
    var statements = module.findFunction("f").block().statements();
    assertEquals(4, statements.size());
    int i = 0;
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression();
      var field = (AstFieldAccess) expr;
      assertEquals(newVariableRef("foo"), field.object());
      assertEquals("bar", field.fieldName());
    }
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression();
      var call = (AstFunctionPointerCall) expr;
      var field = (AstFieldAccess) call.function();
      assertEquals(newVariableRef("foo"), field.object());
      assertEquals("bar", field.fieldName());
    }
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression();
      var index = (AstArrayAccess) expr;
      assertEquals(newVariableRef("foo"), index.array());
      assertEquals(AstNumberLiteral.of(1), index.index());
    }
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression();
      var call = (AstFunctionPointerCall) expr;
      var field = (AstFieldAccess) call.function();
      assertEquals("baz", field.fieldName());
      {
        var index = (AstArrayAccess) field.object();
        assertEquals(AstNumberLiteral.of(3), index.index());
        {
          var nestedCall = (AstFunctionPointerCall) index.array();
          {
            var nestedField = (AstFieldAccess) nestedCall.function();
            assertEquals(newVariableRef("foo"), nestedField.object());
            assertEquals("bar", nestedField.fieldName());
          }
          assertEquals(2, nestedCall.arguments().size());
          assertEquals(AstNumberLiteral.of(1), nestedCall.arguments().get(0));
          assertEquals(AstNumberLiteral.of(2), nestedCall.arguments().get(1));
        }
      }
    }
  }

  @Test
  void testAssignmentExpressionParsing() {
    var code =
        """
        module test;
        function f() {
          a = b = c;
          var x = a + (b = c);
          foo(a = b);
        }
        """;
    var module = parseCode(code);
    var statements = module.findFunction("f").block().statements();
    assertEquals(3, statements.size());
    {
      var expr = ((AstExpressionStatement) statements.get(0)).expression();
      var assign = (AstAssign) expr;
      assertEquals(newVariableRef("a"), assign.target());
      {
        var nestedAssign = (AstAssign) assign.value();
        assertEquals(newVariableRef("b"), nestedAssign.target());
        assertEquals(newVariableRef("c"), nestedAssign.value());
      }
    }
    {
      var expr = ((AstLocal) statements.get(1)).variable().expression();
      var call = (AstCall) expr;
      assertEquals(newBuiltinFunctionRef("+"), call.function());
      assertEquals(newVariableRef("a"), call.arguments().get(0));
      {
        var assign = (AstAssign) call.arguments().get(1);
        assertEquals(newVariableRef("b"), assign.target());
        assertEquals(newVariableRef("c"), assign.value());
      }
    }
    {
      var expr = ((AstExpressionStatement) statements.get(2)).expression();
      var call = (AstCall) expr;
      assertEquals(newFunctionRef("foo"), call.function());
      {
        var assign = (AstAssign) call.arguments().get(0);
        assertEquals(newVariableRef("a"), assign.target());
        assertEquals(newVariableRef("b"), assign.value());
      }
    }
  }

  @Test
  void testCompoundAssignmentExpressionParsing() {
    var code =
        """
        module test;
        function f() {
          a += b -= c;
          var x = a + (b *= c);
          foo(a /= b);
          a %= b;
        }
        """;
    var module = parseCode(code);
    var statements = module.findFunction("f").block().statements();
    assertEquals(4, statements.size());
    {
      var expr = ((AstExpressionStatement) statements.get(0)).expression();
      var assign = (AstCompoundAssign) expr;
      assertEquals(newVariableRef("a"), assign.target());
      assertEquals(newBuiltinFunctionRef("+"), assign.operation());
      {
        var nestedAssign = (AstCompoundAssign) assign.value();
        assertEquals(newVariableRef("b"), nestedAssign.target());
        assertEquals(newBuiltinFunctionRef("-"), nestedAssign.operation());
        assertEquals(newVariableRef("c"), nestedAssign.value());
      }
    }
    {
      var expr = ((AstLocal) statements.get(1)).variable().expression();
      var call = (AstCall) expr;
      assertEquals(newBuiltinFunctionRef("+"), call.function());
      assertEquals(newVariableRef("a"), call.arguments().get(0));
      {
        var assign = (AstCompoundAssign) call.arguments().get(1);
        assertEquals(newVariableRef("b"), assign.target());
        assertEquals(newBuiltinFunctionRef("*"), assign.operation());
        assertEquals(newVariableRef("c"), assign.value());
      }
    }
    {
      var expr = ((AstExpressionStatement) statements.get(2)).expression();
      var call = (AstCall) expr;
      assertEquals(newFunctionRef("foo"), call.function());
      {
        var assign = (AstCompoundAssign) call.arguments().get(0);
        assertEquals(newVariableRef("a"), assign.target());
        assertEquals(newBuiltinFunctionRef("/"), assign.operation());
        assertEquals(newVariableRef("b"), assign.value());
      }
    }
    {
      var expr = ((AstExpressionStatement) statements.get(3)).expression();
      var assign = (AstCompoundAssign) expr;
      assertEquals(newVariableRef("a"), assign.target());
      assertEquals(newBuiltinFunctionRef("%"), assign.operation());
      assertEquals(newVariableRef("b"), assign.value());
    }
  }

  @Test
  void testNewDeleteExpression() {
    var code =
        """
        module test;
        function f() {
          var x = new Duration();
          var y = new Duration[](5);
          delete x;
        }
        """;
    var module = parseCode(code);
    var statements = module.findFunction("f").block().statements();
    {
      var expr = ((AstLocal) statements.get(0)).variable().expression();
      var newExpr = (AstNew) expr;
      assertEquals(new AstPointerType(newTypeRef("Duration")), newExpr.type());
      assertEquals(List.of(), newExpr.arguments());
    }
    {
      var expr = ((AstLocal) statements.get(1)).variable().expression();
      var newExpr = (AstNew) expr;
      assertEquals(new AstArrayType(newTypeRef("Duration")), newExpr.type());
      assertEquals(List.of(AstNumberLiteral.of(5)), newExpr.arguments());
    }
    {
      var stmt = (AstDelete) statements.get(2);
      assertEquals(newVariableRef("x"), stmt.expression());
    }
  }

  @Test
  void testIfStatement() {
    var code =
        """
        module test;
        function f(a bool, b bool) {
          if a {
          }
          if a {
          } else {
          }
          if a {
          } else if b {
          } else {
          }
        }
        """;
    var module = parseCode(code);
    var statements = module.findFunction("f").block().statements();
    int i = 0;
    {
      var stmt = (AstIf) statements.get(i++);
      assertEquals(1, stmt.conditions().size());
      assertEquals(1, stmt.thenBlocks().size());
      assertEquals(newVariableRef("a"), stmt.conditions().get(0));
      assertNull(stmt.elseBlock());
    }
    {
      var stmt = (AstIf) statements.get(i++);
      assertEquals(1, stmt.conditions().size());
      assertEquals(1, stmt.thenBlocks().size());
      assertEquals(newVariableRef("a"), stmt.conditions().get(0));
      assertNotNull(stmt.elseBlock());
    }
    {
      var stmt = (AstIf) statements.get(i++);
      assertEquals(2, stmt.conditions().size());
      assertEquals(2, stmt.thenBlocks().size());
      assertEquals(newVariableRef("a"), stmt.conditions().get(0));
      assertEquals(newVariableRef("b"), stmt.conditions().get(1));
      assertNotNull(stmt.elseBlock());
    }
  }

  @Test
  void testForStatement() {
    var code =
        """
        module test;
        function f() {
          for true {
            break;
          }
          for var i = 0; true {
          }
          for var i = 0; true; i {
          }
        }
        """;
    var module = parseCode(code);
    var statements = module.findFunction("f").block().statements();
    int i = 0;
    {
      var stmt = (AstFor) statements.get(i++);
      assertNull(stmt.init());
      assertEquals(newVariableRef("true"), stmt.condition());
      assertNull(stmt.increment());
      var bodyStatements = stmt.block().statements();
      assertEquals(1, bodyStatements.size());
      assertEquals(new AstBreak(null), bodyStatements.get(0));
    }
    {
      var stmt = (AstFor) statements.get(i++);
      var init = stmt.init().variable();
      assertEquals(new Identifier("i"), init.name());
      assertNull(init.type());
      assertEquals(AstNumberLiteral.of(0), init.expression());
      assertEquals(newVariableRef("true"), stmt.condition());
      assertNull(stmt.increment());
      assertEquals(ImmutableList.of(), stmt.block().statements());
    }
    {
      var stmt = (AstFor) statements.get(i++);
      var init = stmt.init().variable();
      assertEquals(new Identifier("i"), init.name());
      assertNull(init.type());
      assertEquals(AstNumberLiteral.of(0), init.expression());
      assertEquals(newVariableRef("true"), stmt.condition());
      assertEquals(newVariableRef("i"), stmt.increment());
      assertEquals(ImmutableList.of(), stmt.block().statements());
    }
  }

  @Test
  void testForStatementDanglingSemicolonNotAllowed() {
    {
      var code =
          """
          module test;
          function f() {
            for var i = 0; {
            }
          }
          """;
      assertThrows(CompilerException.class, () -> parseCode(code));
    }
    {
      var code =
          """
          module test;
          function f() {
            for var i = 0; true; {
            }
          }
          """;
      assertThrows(CompilerException.class, () -> parseCode(code));
    }
    {
      var code =
          """
          module test;
          function f() {
            for var i = 0; true; i; {
            }
          }
          """;
      assertThrows(CompilerException.class, () -> parseCode(code));
    }
  }

  @Test
  void testFunctionParameterListEndingComma() {
    var code =
        """
        module test;
        function f(a int,) {}
        """;
    assertThrows(CompilerException.class, () -> parseCode(code));
  }

  @Test
  void testReturnStatement() {
    var code =
        """
        module test;
        function f() {
          return value;
        }
        """;
    var module = parseCode(code);
    var statements = module.findFunction("f").block().statements();
    {
      var stmt = (AstReturn) statements.get(0);
      assertEquals(newVariableRef("value"), stmt.expression());
    }
  }

  @Test
  void testDeferStatement() {
    var code =
        """
        module test;
        function f() {
          defer value;
          defer {
            value;
          }
        }
        """;
    var module = parseCode(code);
    var statements = module.findFunction("f").block().statements();
    {
      var stmt = (AstDefer) statements.get(0);
      assertEquals(1, stmt.block().statements().size());
      var exprStmt = (AstExpressionStatement) stmt.block().statements().get(0);
      assertEquals(newVariableRef("value"), exprStmt.expression());
    }
    {
      var stmt = (AstDefer) statements.get(1);
      assertEquals(1, stmt.block().statements().size());
      var exprStmt = (AstExpressionStatement) stmt.block().statements().get(0);
      assertEquals(newVariableRef("value"), exprStmt.expression());
    }
  }
}
