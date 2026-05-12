package com.github.idemura.cimple.compiler.parser;

import static com.github.idemura.cimple.compiler.ast.AstUtils.*;
import static com.github.idemura.cimple.compiler.parser.Parser.parseCode;
import static org.junit.jupiter.api.Assertions.*;

import com.github.idemura.cimple.compiler.CompilerException;
import com.github.idemura.cimple.compiler.ErrorConsumer;
import com.github.idemura.cimple.compiler.Identifier;
import com.github.idemura.cimple.compiler.InMemoryErrorConsumer;
import com.github.idemura.cimple.compiler.ast.AstArrayAccess;
import com.github.idemura.cimple.compiler.ast.AstCall;
import com.github.idemura.cimple.compiler.ast.AstCast;
import com.github.idemura.cimple.compiler.ast.AstDefer;
import com.github.idemura.cimple.compiler.ast.AstDelete;
import com.github.idemura.cimple.compiler.ast.AstEntityRef;
import com.github.idemura.cimple.compiler.ast.AstExpressionStatement;
import com.github.idemura.cimple.compiler.ast.AstFieldAccess;
import com.github.idemura.cimple.compiler.ast.AstFor;
import com.github.idemura.cimple.compiler.ast.AstFunctionType;
import com.github.idemura.cimple.compiler.ast.AstGoto;
import com.github.idemura.cimple.compiler.ast.AstIf;
import com.github.idemura.cimple.compiler.ast.AstLocal;
import com.github.idemura.cimple.compiler.ast.AstNew;
import com.github.idemura.cimple.compiler.ast.AstNumberLiteral;
import com.github.idemura.cimple.compiler.ast.AstReceiverLookup;
import com.github.idemura.cimple.compiler.ast.AstRecordType;
import com.github.idemura.cimple.compiler.ast.AstReturn;
import com.github.idemura.cimple.compiler.ast.AstUnionType;
import com.github.idemura.cimple.compiler.ast.AstVariable;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

class ParserTest {
  private static ErrorConsumer makeErrorConsumer() {
    var errorConsumer = new InMemoryErrorConsumer();
    errorConsumer.enable(ErrorConsumer.Mode.THROW_ON_ERROR);
    return errorConsumer;
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
        const c0 int = 7;
        function f0() {}
        function f1(x int) {}
        function f2(x int, y int) {}
        function rv() int {
          return 1;
        }
        """;
    var module = parseCode(code, makeErrorConsumer());
    assertEquals("test", module.name());
    {
      var f = module.findFunction("f0");
      assertEquals(Identifier.ofEntity("f0"), f.name());
      assertNull(f.header().resultType());
      assertEquals(ImmutableList.of(), f.header().parameters());
    }
    {
      var f = module.findFunction("f1");
      assertEquals(Identifier.ofEntity("f1"), f.name());
      assertNull(f.header().resultType());
      var params = f.header().parameters();
      assertEquals(1, params.size());
      assertEquals(rawVariable("x", "int"), params.get(0));
    }
    {
      var f = module.findFunction("f2");
      assertEquals(Identifier.ofEntity("f2"), f.name());
      assertNull(f.header().resultType());
      var params = f.header().parameters();
      assertEquals(2, params.size());
      assertEquals(rawVariable("x", "int"), params.get(0));
      assertEquals(rawVariable("y", "int"), params.get(1));
    }
    {
      var f = module.findFunction("rv");
      assertEquals(Identifier.ofEntity("rv"), f.name());
      assertEquals(newTypeRef("int"), f.header().resultType());
      assertEquals(ImmutableList.of(), f.header().parameters());
    }
    {
      var v = module.findVariable("v0");
      assertEquals(Identifier.ofEntity("v0"), v.name());
      assertEquals(newTypeRef("int"), v.type());
      assertTrue(v.getBit(AstVariable.MUTABLE));
    }
    {
      var v = module.findVariable("v1");
      assertEquals(Identifier.ofEntity("v1"), v.name());
      assertEquals(newTypeRef("int"), v.type());
      assertTrue(v.getBit(AstVariable.MUTABLE));
    }
    {
      var v = module.findVariable("v2");
      assertEquals(Identifier.ofEntity("v2"), v.name());
      assertNull(v.type());
      assertTrue(v.getBit(AstVariable.MUTABLE));
    }
    {
      var v = module.findVariable("p");
      assertEquals(Identifier.ofEntity("p"), v.name());
      assertEquals(pointerType(newTypeRef("int")), v.type());
      assertTrue(v.getBit(AstVariable.MUTABLE));
    }
    {
      var v = module.findVariable("pp");
      assertEquals(Identifier.ofEntity("pp"), v.name());
      assertEquals(pointerType(pointerType(newTypeRef("int"))), v.type());
      assertTrue(v.getBit(AstVariable.MUTABLE));
    }
    {
      var v = module.findVariable("c0");
      assertEquals(Identifier.ofEntity("c0"), v.name());
      assertEquals(newTypeRef("int"), v.type());
      assertFalse(v.getBit(AstVariable.MUTABLE));
    }
  }

  @Test
  void testRecordType() {
    var code =
        """
        module test;
        type record Empty {}
        type record Point {
          var x int;
          var y int;
          const name string;
        }
        """;
    var module = parseCode(code, makeErrorConsumer());
    assertEquals("test", module.name());
    {
      var type = (AstRecordType) module.findType("Empty");
      assertEquals(Identifier.ofType("Empty"), type.name());
      assertEquals(ImmutableList.of(), type.fields());
    }
    {
      var type = (AstRecordType) module.findType("Point");
      assertEquals(Identifier.ofType("Point"), type.name());
      var fields = type.fields();
      assertEquals(3, fields.size());
      int j = 0;
      {
        var f = fields.get(j++);
        assertEquals(Identifier.ofEntity("x"), f.name());
        assertEquals(newTypeRef("int"), f.type());
        assertTrue(f.getBit(AstVariable.MUTABLE));
      }
      {
        var f = fields.get(j++);
        assertEquals(Identifier.ofEntity("y"), f.name());
        assertEquals(newTypeRef("int"), f.type());
        assertTrue(f.getBit(AstVariable.MUTABLE));
      }
      {
        var f = fields.get(j++);
        assertEquals(Identifier.ofEntity("name"), f.name());
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
    var module = parseCode(code, makeErrorConsumer());
    assertEquals("test", module.name());
    var type = (AstUnionType) module.findType("Maybe");
    assertEquals(Identifier.ofType("Maybe"), type.name());
    assertEquals(
        ImmutableList.of(unionVariant("None", null), unionVariant("Some", "string")),
        type.variants());
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
    var module = parseCode(code, makeErrorConsumer());
    assertEquals("test", module.name());
    {
      var type = (AstFunctionType) module.findType("Compare");
      assertEquals(Identifier.ofType("Compare"), type.name());
      assertEquals(newTypeRef("bool"), type.header().resultType());
      var params = type.header().parameters();
      assertEquals(2, params.size());
      assertEquals(rawVariable("a", "int"), params.get(0));
      assertEquals(rawVariable("b", "int"), params.get(1));
    }
    {
      var type = (AstFunctionType) module.findType("Supplier");
      assertEquals(Identifier.ofType("Supplier"), type.name());
      assertEquals(newTypeRef("string"), type.header().resultType());
      assertEquals(ImmutableList.of(), type.header().parameters());
    }
    {
      var type = (AstFunctionType) module.findType("Consumer");
      assertEquals(Identifier.ofType("Consumer"), type.name());
      assertNull(type.header().resultType());
      assertEquals(ImmutableList.of(rawVariable("v", "string")), type.header().parameters());
    }
  }

  @Test
  void testReceiverFunction() {
    var code =
        """
        module test;
        function Duration:toMillis(this) int {
          return 6;
        }
        """;
    var module = parseCode(code, makeErrorConsumer());
    var header = module.findReceiverFunction("Duration", "toMillis").header();
    assertEquals(newTypeRef("Duration"), header.receiverType());
    assertEquals(
        Identifier.ofTypeEntity("Duration", "toMillis"),
        module.findReceiverFunction("Duration", "toMillis").name());
    assertEquals(newTypeRef("int"), header.resultType());
    assertEquals(ImmutableList.of(rawVariable("this")), header.parameters());
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
          var x = [1 + 2 type int];
          var x = p.t * 5;
        }
        """;
    var module = parseCode(code, makeErrorConsumer());
    var statements = module.findFunction("f").block().statements();
    int i = 0;
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression().value();
      assertEquals(AstNumberLiteral.of(1), expr);
    }
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression().value();
      {
        var call = (AstCall) expr;
        assertEquals(newBuiltinEntityRef("+"), call.function());
        assertEquals(2, call.arguments().size());
        assertEquals(AstNumberLiteral.of(1), call.arguments().get(0));
        assertEquals(AstNumberLiteral.of(2), call.arguments().get(1));
      }
    }
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression().value();
      {
        var callSub = (AstCall) expr;
        assertEquals(newBuiltinEntityRef("-"), callSub.function());
        assertEquals(2, callSub.arguments().size());
        {
          var callAdd = (AstCall) callSub.arguments().get(0);
          assertEquals(newBuiltinEntityRef("+"), callAdd.function());
          assertEquals(2, callAdd.arguments().size());
          assertEquals(AstNumberLiteral.of(1), callAdd.arguments().get(0));
          assertEquals(AstNumberLiteral.of(2), callAdd.arguments().get(1));
        }
        assertEquals(AstNumberLiteral.of(3), callSub.arguments().get(1));
      }
    }
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression().value();
      {
        var callMul = (AstCall) expr;
        assertEquals(newBuiltinEntityRef("*"), callMul.function());
        assertEquals(2, callMul.arguments().size());
        assertEquals(AstNumberLiteral.of(1), callMul.arguments().get(0));
        assertEquals(AstNumberLiteral.of(2), callMul.arguments().get(1));
      }
    }
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression().value();
      {
        var callDiv = (AstCall) expr;
        assertEquals(newBuiltinEntityRef("/"), callDiv.function());
        assertEquals(2, callDiv.arguments().size());
        assertEquals(AstNumberLiteral.of(1), callDiv.arguments().get(0));
        assertEquals(AstNumberLiteral.of(2), callDiv.arguments().get(1));
      }
    }
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression().value();
      {
        var callMod = (AstCall) expr;
        assertEquals(newBuiltinEntityRef("%"), callMod.function());
        assertEquals(2, callMod.arguments().size());
        assertEquals(AstNumberLiteral.of(1), callMod.arguments().get(0));
        assertEquals(AstNumberLiteral.of(2), callMod.arguments().get(1));
      }
    }
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression().value();
      {
        var callMul = (AstCall) expr;
        assertEquals(newBuiltinEntityRef("*"), callMul.function());
        assertEquals(2, callMul.arguments().size());
        {
          var nestedCallMul = (AstCall) callMul.arguments().get(0);
          assertEquals(newBuiltinEntityRef("*"), nestedCallMul.function());
          assertEquals(2, nestedCallMul.arguments().size());
          assertEquals(AstNumberLiteral.of(1), nestedCallMul.arguments().get(0));
          assertEquals(AstNumberLiteral.of(2), nestedCallMul.arguments().get(1));
        }
        assertEquals(AstNumberLiteral.of(3), callMul.arguments().get(1));
      }
    }
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression().value();
      {
        var callAdd = (AstCall) expr;
        assertEquals(newBuiltinEntityRef("+"), callAdd.function());
        assertEquals(2, callAdd.arguments().size());
        assertEquals(AstNumberLiteral.of(1), callAdd.arguments().get(0));
        {
          var callMul = (AstCall) callAdd.arguments().get(1);
          assertEquals(newBuiltinEntityRef("*"), callMul.function());
          assertEquals(2, callMul.arguments().size());
          assertEquals(AstNumberLiteral.of(2), callMul.arguments().get(0));
          assertEquals(AstNumberLiteral.of(3), callMul.arguments().get(1));
        }
      }
    }
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression().value();
      {
        var callAdd = (AstCall) expr;
        assertEquals(newBuiltinEntityRef("+"), callAdd.function());
        assertEquals(2, callAdd.arguments().size());
        {
          var callMul = (AstCall) callAdd.arguments().get(0);
          assertEquals(newBuiltinEntityRef("*"), callMul.function());
          assertEquals(2, callMul.arguments().size());
          assertEquals(AstNumberLiteral.of(1), callMul.arguments().get(0));
          assertEquals(AstNumberLiteral.of(2), callMul.arguments().get(1));
        }
        assertEquals(AstNumberLiteral.of(3), callAdd.arguments().get(1));
      }
    }
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression().value();
      {
        var call = (AstCall) expr;
        assertEquals(newBuiltinEntityRef("+"), call.function());
        assertEquals(2, call.arguments().size());
        assertEquals(AstNumberLiteral.of(1), call.arguments().get(0));
        assertEquals(AstNumberLiteral.of(2), call.arguments().get(1));
      }
    }
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression().value();
      {
        var cast = (AstCast) expr;
        assertEquals(newTypeRef("int"), cast.type());
        var callAdd = (AstCall) cast.expression();
        assertEquals(newBuiltinEntityRef("+"), callAdd.function());
        assertEquals(2, callAdd.arguments().size());
        assertEquals(AstNumberLiteral.of(1), callAdd.arguments().get(0));
        assertEquals(AstNumberLiteral.of(2), callAdd.arguments().get(1));
      }
    }
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression().value();
      {
        var call = (AstCall) expr;
        assertEquals(newBuiltinEntityRef("*"), call.function());
        {
          var fieldAccess = (AstFieldAccess) call.arguments().get(0);
          assertEquals(newEntityRef("p"), fieldAccess.object());
          assertEquals("t", fieldAccess.fieldName());
        }
        assertEquals(AstNumberLiteral.of(5), call.arguments().get(1));
      }
    }
  }

  @Test
  void testInvokeExpression() {
    // We allow `(foo)()` because there is no ambiguity with type casts in Ci.
    // Casts use the dedicated form `[<expression> type <cast-type>]`.
    var code =
        """
        module test;
        function f() {
          var x = foo~bar;
          var x = foo();
          var x = (foo)();
          var x = foo(1, 2);
          var x = foo~bar();
        }
        """;
    var module = parseCode(code, makeErrorConsumer());
    var statements = module.findFunction("f").block().statements();
    int i = 0;
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression().value();
      var entityRef = (AstEntityRef) expr;
      assertEquals(newEntityRef("foo", "bar"), entityRef);
    }
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression().value();
      var call = (AstCall) expr;
      assertEquals(newEntityRef("foo"), call.function());
      assertEquals(ImmutableList.of(), call.arguments());
    }
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression().value();
      var call = (AstCall) expr;
      assertEquals(newEntityRef("foo"), call.function());
      assertEquals(ImmutableList.of(), call.arguments());
    }
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression().value();
      var call = (AstCall) expr;
      assertEquals(newEntityRef("foo"), call.function());
      assertEquals(2, call.arguments().size());
      assertEquals(AstNumberLiteral.of(1), call.arguments().get(0));
      assertEquals(AstNumberLiteral.of(2), call.arguments().get(1));
    }
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression().value();
      var call = (AstCall) expr;
      assertEquals(newEntityRef("foo", "bar"), call.function());
      assertEquals(ImmutableList.of(), call.arguments());
    }
  }

  @Test
  void testFieldArrayCallChain() {
    var code =
        """
        module test;
        function f() {
          var x = foo.bar;
          var x = foo:bar;
          var x = foo[1];
          var x = foo.bar(1, 2)[3]:baz;
        }
        """;
    var module = parseCode(code, makeErrorConsumer());
    var statements = module.findFunction("f").block().statements();
    int i = 0;
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression().value();
      var field = (AstFieldAccess) expr;
      assertEquals(newEntityRef("foo"), field.object());
      assertEquals("bar", field.fieldName());
    }
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression().value();
      var receiverLookup = (AstReceiverLookup) expr;
      assertEquals(newEntityRef("foo"), receiverLookup.receiver());
      assertEquals("bar", receiverLookup.functionName());
    }
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression().value();
      var index = (AstArrayAccess) expr;
      assertEquals(newEntityRef("foo"), index.array());
      assertEquals(AstNumberLiteral.of(1), index.index());
    }
    {
      var expr = ((AstLocal) statements.get(i++)).variable().expression().value();
      var receiverLookup = (AstReceiverLookup) expr;
      assertEquals("baz", receiverLookup.functionName());
      {
        var index = (AstArrayAccess) receiverLookup.receiver();
        assertEquals(AstNumberLiteral.of(3), index.index());
        {
          var call = (AstCall) index.array();
          {
            var field = (AstFieldAccess) call.function();
            assertEquals(newEntityRef("foo"), field.object());
            assertEquals("bar", field.fieldName());
          }
          assertEquals(2, call.arguments().size());
          assertEquals(AstNumberLiteral.of(1), call.arguments().get(0));
          assertEquals(AstNumberLiteral.of(2), call.arguments().get(1));
        }
      }
    }
  }

  @Test
  void testNewDeleteExpression() {
    var code =
        """
        module test;
        function f() {
          var x = new Duration;
          var y = new Duration[5];
          delete x;
        }
        """;
    var module = parseCode(code, makeErrorConsumer());
    var statements = module.findFunction("f").block().statements();
    {
      var expr = ((AstLocal) statements.get(0)).variable().expression().value();
      var newExpr = (AstNew) expr;
      assertEquals(newTypeRef("Duration"), newExpr.type());
      assertNull(newExpr.size());
    }
    {
      var expr = ((AstLocal) statements.get(1)).variable().expression().value();
      var newExpr = (AstNew) expr;
      assertEquals(newTypeRef("Duration"), newExpr.type());
      assertEquals(AstNumberLiteral.of(5), newExpr.size());
    }
    {
      var stmt = (AstDelete) statements.get(2);
      assertEquals(newEntityRef("x"), stmt.expression().value());
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
    var module = parseCode(code, makeErrorConsumer());
    var statements = module.findFunction("f").block().statements();
    int i = 0;
    {
      var stmt = (AstIf) statements.get(i++);
      assertEquals(1, stmt.conditions().size());
      assertEquals(1, stmt.thenBlocks().size());
      assertEquals(newEntityRef("a"), stmt.conditions().get(0).value());
      assertNull(stmt.elseBlock());
    }
    {
      var stmt = (AstIf) statements.get(i++);
      assertEquals(1, stmt.conditions().size());
      assertEquals(1, stmt.thenBlocks().size());
      assertEquals(newEntityRef("a"), stmt.conditions().get(0).value());
      assertNotNull(stmt.elseBlock());
    }
    {
      var stmt = (AstIf) statements.get(i++);
      assertEquals(2, stmt.conditions().size());
      assertEquals(2, stmt.thenBlocks().size());
      assertEquals(newEntityRef("a"), stmt.conditions().get(0).value());
      assertEquals(newEntityRef("b"), stmt.conditions().get(1).value());
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
            goto end;
          }
          for var i = 0; true {
          }
          for var i = 0; true; i {
          }
        }
        """;
    var module = parseCode(code, makeErrorConsumer());
    var statements = module.findFunction("f").block().statements();
    int i = 0;
    {
      var stmt = (AstFor) statements.get(i++);
      assertNull(stmt.init());
      assertEquals(newEntityRef("true"), stmt.condition().value());
      assertNull(stmt.increment());
      var bodyStatements = stmt.block().statements();
      assertEquals(1, bodyStatements.size());
      assertEquals(new AstGoto("end"), bodyStatements.get(0));
    }
    {
      var stmt = (AstFor) statements.get(i++);
      var init = stmt.init();
      assertEquals(Identifier.ofEntity("i"), init.name());
      assertNull(init.type());
      assertEquals(AstNumberLiteral.of(0), init.expression().value());
      assertEquals(newEntityRef("true"), stmt.condition().value());
      assertNull(stmt.increment());
      assertEquals(ImmutableList.of(), stmt.block().statements());
    }
    {
      var stmt = (AstFor) statements.get(i++);
      var init = stmt.init();
      assertEquals(Identifier.ofEntity("i"), init.name());
      assertNull(init.type());
      assertEquals(AstNumberLiteral.of(0), init.expression().value());
      assertEquals(newEntityRef("true"), stmt.condition().value());
      assertEquals(newEntityRef("i"), stmt.increment().value());
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
      assertThrows(CompilerException.class, () -> parseCode(code, makeErrorConsumer()));
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
      assertThrows(CompilerException.class, () -> parseCode(code, makeErrorConsumer()));
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
      assertThrows(CompilerException.class, () -> parseCode(code, makeErrorConsumer()));
    }
  }

  @Test
  void testFunctionParameterListEndingComma() {
    var code =
        """
        module test;
        function f(a int,) {}
        """;
    assertThrows(CompilerException.class, () -> parseCode(code, makeErrorConsumer()));
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
    var module = parseCode(code, makeErrorConsumer());
    var statements = module.findFunction("f").block().statements();
    {
      var stmt = (AstReturn) statements.get(0);
      assertEquals(newEntityRef("value"), stmt.expression().value());
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
    var module = parseCode(code, makeErrorConsumer());
    var statements = module.findFunction("f").block().statements();
    {
      var stmt = (AstDefer) statements.get(0);
      assertEquals(1, stmt.block().statements().size());
      var exprStmt = (AstExpressionStatement) stmt.block().statements().get(0);
      assertEquals(newEntityRef("value"), exprStmt.expression().value());
    }
    {
      var stmt = (AstDefer) statements.get(1);
      assertEquals(1, stmt.block().statements().size());
      var exprStmt = (AstExpressionStatement) stmt.block().statements().get(0);
      assertEquals(newEntityRef("value"), exprStmt.expression().value());
    }
  }
}
