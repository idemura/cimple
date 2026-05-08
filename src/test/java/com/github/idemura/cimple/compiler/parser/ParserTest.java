package com.github.idemura.cimple.compiler.parser;

import static com.github.idemura.cimple.compiler.Constants.BUILTIN_MODULE;
import static com.github.idemura.cimple.compiler.parser.Parser.parseCode;
import static org.junit.jupiter.api.Assertions.*;

import com.github.idemura.cimple.compiler.CompilerException;
import com.github.idemura.cimple.compiler.ErrorConsumer;
import com.github.idemura.cimple.compiler.InMemoryErrorConsumer;
import com.github.idemura.cimple.compiler.QualifiedName;
import com.github.idemura.cimple.compiler.ast.AstArrayAccess;
import com.github.idemura.cimple.compiler.ast.AstBind;
import com.github.idemura.cimple.compiler.ast.AstCall;
import com.github.idemura.cimple.compiler.ast.AstCast;
import com.github.idemura.cimple.compiler.ast.AstDefer;
import com.github.idemura.cimple.compiler.ast.AstEntityRef;
import com.github.idemura.cimple.compiler.ast.AstExpressionStatement;
import com.github.idemura.cimple.compiler.ast.AstFieldAccess;
import com.github.idemura.cimple.compiler.ast.AstFor;
import com.github.idemura.cimple.compiler.ast.AstFunction;
import com.github.idemura.cimple.compiler.ast.AstFunctionType;
import com.github.idemura.cimple.compiler.ast.AstGoto;
import com.github.idemura.cimple.compiler.ast.AstIf;
import com.github.idemura.cimple.compiler.ast.AstLocal;
import com.github.idemura.cimple.compiler.ast.AstModule;
import com.github.idemura.cimple.compiler.ast.AstNumberLiteral;
import com.github.idemura.cimple.compiler.ast.AstRecordType;
import com.github.idemura.cimple.compiler.ast.AstReturn;
import com.github.idemura.cimple.compiler.ast.AstType;
import com.github.idemura.cimple.compiler.ast.AstTypeRef;
import com.github.idemura.cimple.compiler.ast.AstUnionType;
import com.github.idemura.cimple.compiler.ast.AstVariable;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ParserTest {
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

  private static ErrorConsumer makeErrorConsumer() {
    var errorConsumer = new InMemoryErrorConsumer();
    errorConsumer.enable(ErrorConsumer.Mode.THROW_ON_ERROR);
    return errorConsumer;
  }

  private static AstVariable parameter(String name, String typeName) {
    var parameter = new AstVariable();
    parameter.setName(new QualifiedName(name));
    var type = new AstTypeRef();
    type.setName(new QualifiedName(typeName));
    parameter.setType(type);
    parameter.setBit(AstVariable.PARAM);
    return parameter;
  }

  private static AstUnionType.Variant unionVariant(String name, String typeName) {
    var unionVariant = new AstUnionType.Variant();
    unionVariant.setTag(name);
    if (typeName != null) {
      unionVariant.setValueType(AstTypeRef.ofName(typeName));
    }
    return unionVariant;
  }

  private static AstEntityRef builtinEntityRef(String symbol) {
    return AstEntityRef.ofName(BUILTIN_MODULE, symbol);
  }

  @Test
  void testModule() {
    var code =
        """
        module test;

        var v0 int = 5;
        var v1 int;
        var v2 = 5;
        const c0 int = 7;

        function f0() {}
        function f1(x int) {}
        function f2(x int, y int) {}

        function rv() int {
          return 1;
        }
        """;
    var module = parseCode(code, makeErrorConsumer());
    assertEquals("test", module.getName());
    var functions = functions(module);
    assertEquals(4, functions.size());
    {
      var f = functions.get(0);
      assertEquals(new QualifiedName("f0"), f.getHeader().getName());
      assertNull(f.getHeader().getResultType());
      assertEquals(ImmutableList.of(), f.getHeader().getParameters());
    }
    {
      var f = functions.get(1);
      assertEquals(new QualifiedName("f1"), f.getHeader().getName());
      assertNull(f.getHeader().getResultType());
      var params = f.getHeader().getParameters();
      assertEquals(1, params.size());
      assertEquals(parameter("x", "int"), params.get(0));
    }
    {
      var f = functions.get(2);
      assertEquals(new QualifiedName("f2"), f.getHeader().getName());
      assertNull(f.getHeader().getResultType());
      var params = f.getHeader().getParameters();
      assertEquals(2, params.size());
      assertEquals(parameter("x", "int"), params.get(0));
      assertEquals(parameter("y", "int"), params.get(1));
    }
    {
      var f = functions.get(3);
      assertEquals(new QualifiedName("rv"), f.getHeader().getName());
      assertEquals(AstTypeRef.ofName("int"), f.getHeader().getResultType());
      assertEquals(ImmutableList.of(), f.getHeader().getParameters());
    }
    var variables = variables(module);
    assertEquals(4, variables.size());
    {
      var v = variables.get(0);
      assertEquals(new QualifiedName("v0"), v.getName());
      assertEquals(AstTypeRef.ofName("int"), v.getType());
      assertTrue(v.getBit(AstVariable.MUTABLE));
    }
    {
      var v = variables.get(1);
      assertEquals(new QualifiedName("v1"), v.getName());
      assertEquals(AstTypeRef.ofName("int"), v.getType());
      assertTrue(v.getBit(AstVariable.MUTABLE));
    }
    {
      var v = variables.get(2);
      assertEquals(new QualifiedName("v2"), v.getName());
      assertNull(v.getType());
      assertTrue(v.getBit(AstVariable.MUTABLE));
    }
    {
      var v = variables.get(3);
      assertEquals(new QualifiedName("c0"), v.getName());
      assertEquals(AstTypeRef.ofName("int"), v.getType());
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
    assertEquals("test", module.getName());
    var types = types(module);
    assertEquals(2, types.size());
    {
      var type = (AstRecordType) types.get(0);
      assertEquals(new QualifiedName("Empty"), type.getName());
      assertEquals(ImmutableList.of(), type.getFields());
    }
    {
      var type = (AstRecordType) types.get(1);
      assertEquals(new QualifiedName("Point"), type.getName());
      var fields = type.getFields();
      assertEquals(3, fields.size());
      int j = 0;
      {
        var f = fields.get(j++);
        assertEquals(new QualifiedName("x"), f.getName());
        assertEquals(AstTypeRef.ofName("int"), f.getType());
        assertTrue(f.getBit(AstVariable.MUTABLE));
      }
      {
        var f = fields.get(j++);
        assertEquals(new QualifiedName("y"), f.getName());
        assertEquals(AstTypeRef.ofName("int"), f.getType());
        assertTrue(f.getBit(AstVariable.MUTABLE));
      }
      {
        var f = fields.get(j++);
        assertEquals(new QualifiedName("name"), f.getName());
        assertEquals(AstTypeRef.ofName("string"), f.getType());
        assertFalse(f.getBit(AstVariable.MUTABLE));
      }
    }
  }

  @Test
  void testUnionType() {
    var code =
        """
        module test;
        type union Option {
          None;
          Some(string);
        }
        """;
    var module = parseCode(code, makeErrorConsumer());
    assertEquals("test", module.getName());
    var types = types(module);
    assertEquals(1, types.size());
    var type = (AstUnionType) types.get(0);
    assertEquals(new QualifiedName("Option"), type.getName());
    assertEquals(
        ImmutableList.of(unionVariant("None", null), unionVariant("Some", "string")),
        type.getVariants());
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
    assertEquals("test", module.getName());
    var types = types(module);
    assertEquals(3, types.size());
    {
      var type = (AstFunctionType) types.get(0);
      assertEquals(new QualifiedName("Compare"), type.getName());
      assertEquals(AstTypeRef.ofName("bool"), type.getHeader().getResultType());
      var params = type.getHeader().getParameters();
      assertEquals(2, params.size());
      assertEquals(parameter("a", "int"), params.get(0));
      assertEquals(parameter("b", "int"), params.get(1));
    }
    {
      var type = (AstFunctionType) types.get(1);
      assertEquals(new QualifiedName("Supplier"), type.getName());
      assertEquals(AstTypeRef.ofName("string"), type.getHeader().getResultType());
      assertEquals(ImmutableList.of(), type.getHeader().getParameters());
    }
    {
      var type = (AstFunctionType) types.get(2);
      assertEquals(new QualifiedName("Consumer"), type.getName());
      assertNull(type.getHeader().getResultType());
      assertEquals(ImmutableList.of(parameter("v", "string")), type.getHeader().getParameters());
    }
  }

  @Test
  void testReceiverFunction() {
    var code =
        """
        module test;

        function Duration:toMillis(this int) int {
          return this;
        }
        """;
    var module = parseCode(code, makeErrorConsumer());
    var functions = functions(module);
    assertEquals(1, functions.size());
    var header = functions.get(0).getHeader();
    assertEquals(AstTypeRef.ofName("Duration"), header.getReceiverType());
    assertEquals(new QualifiedName("toMillis"), header.getName());
    assertEquals(AstTypeRef.ofName("int"), header.getResultType());
    assertEquals(ImmutableList.of(parameter("this", "int")), header.getParameters());
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
          var x = 1 * 2 * 3;
          var x = 1 + 2 * 3;
          var x = 1 * 2 + 3;
          var x = (1 + 2);
          var x = [1 + 2 type int];
          var x = p.t * 5;
        }
        """;
    var module = parseCode(code, makeErrorConsumer());
    var statements = functions(module).get(0).getBlock().statements();
    int i = 0;
    {
      var expr = ((AstLocal) statements.get(i++)).getVariable().getExpression();
      assertEquals(AstNumberLiteral.of(1), expr);
    }
    {
      var expr = ((AstLocal) statements.get(i++)).getVariable().getExpression();
      {
        var call = (AstCall) expr;
        assertEquals(builtinEntityRef("+"), call.getFunction());
        assertEquals(2, call.getArgs().size());
        assertEquals(AstNumberLiteral.of(1), call.getArgs().get(0));
        assertEquals(AstNumberLiteral.of(2), call.getArgs().get(1));
      }
    }
    {
      var expr = ((AstLocal) statements.get(i++)).getVariable().getExpression();
      {
        var callSub = (AstCall) expr;
        assertEquals(builtinEntityRef("-"), callSub.getFunction());
        assertEquals(2, callSub.getArgs().size());
        {
          var callAdd = (AstCall) callSub.getArgs().get(0);
          assertEquals(builtinEntityRef("+"), callAdd.getFunction());
          assertEquals(2, callAdd.getArgs().size());
          assertEquals(AstNumberLiteral.of(1), callAdd.getArgs().get(0));
          assertEquals(AstNumberLiteral.of(2), callAdd.getArgs().get(1));
        }
        assertEquals(AstNumberLiteral.of(3), callSub.getArgs().get(1));
      }
    }
    {
      var expr = ((AstLocal) statements.get(i++)).getVariable().getExpression();
      {
        var callMul = (AstCall) expr;
        assertEquals(builtinEntityRef("*"), callMul.getFunction());
        assertEquals(2, callMul.getArgs().size());
        assertEquals(AstNumberLiteral.of(1), callMul.getArgs().get(0));
        assertEquals(AstNumberLiteral.of(2), callMul.getArgs().get(1));
      }
    }
    {
      var expr = ((AstLocal) statements.get(i++)).getVariable().getExpression();
      {
        var callMul = (AstCall) expr;
        assertEquals(builtinEntityRef("*"), callMul.getFunction());
        assertEquals(2, callMul.getArgs().size());
        {
          var nestedCallMul = (AstCall) callMul.getArgs().get(0);
          assertEquals(builtinEntityRef("*"), nestedCallMul.getFunction());
          assertEquals(2, nestedCallMul.getArgs().size());
          assertEquals(AstNumberLiteral.of(1), nestedCallMul.getArgs().get(0));
          assertEquals(AstNumberLiteral.of(2), nestedCallMul.getArgs().get(1));
        }
        assertEquals(AstNumberLiteral.of(3), callMul.getArgs().get(1));
      }
    }
    {
      var expr = ((AstLocal) statements.get(i++)).getVariable().getExpression();
      {
        var callAdd = (AstCall) expr;
        assertEquals(builtinEntityRef("+"), callAdd.getFunction());
        assertEquals(2, callAdd.getArgs().size());
        assertEquals(AstNumberLiteral.of(1), callAdd.getArgs().get(0));
        {
          var callMul = (AstCall) callAdd.getArgs().get(1);
          assertEquals(builtinEntityRef("*"), callMul.getFunction());
          assertEquals(2, callMul.getArgs().size());
          assertEquals(AstNumberLiteral.of(2), callMul.getArgs().get(0));
          assertEquals(AstNumberLiteral.of(3), callMul.getArgs().get(1));
        }
      }
    }
    {
      var expr = ((AstLocal) statements.get(i++)).getVariable().getExpression();
      {
        var callAdd = (AstCall) expr;
        assertEquals(builtinEntityRef("+"), callAdd.getFunction());
        assertEquals(2, callAdd.getArgs().size());
        {
          var callMul = (AstCall) callAdd.getArgs().get(0);
          assertEquals(builtinEntityRef("*"), callMul.getFunction());
          assertEquals(2, callMul.getArgs().size());
          assertEquals(AstNumberLiteral.of(1), callMul.getArgs().get(0));
          assertEquals(AstNumberLiteral.of(2), callMul.getArgs().get(1));
        }
        assertEquals(AstNumberLiteral.of(3), callAdd.getArgs().get(1));
      }
    }
    {
      var expr = ((AstLocal) statements.get(i++)).getVariable().getExpression();
      {
        var call = (AstCall) expr;
        assertEquals(builtinEntityRef("+"), call.getFunction());
        assertEquals(2, call.getArgs().size());
        assertEquals(AstNumberLiteral.of(1), call.getArgs().get(0));
        assertEquals(AstNumberLiteral.of(2), call.getArgs().get(1));
      }
    }
    {
      var expr = ((AstLocal) statements.get(i++)).getVariable().getExpression();
      {
        var cast = (AstCast) expr;
        assertEquals(AstTypeRef.ofName("int"), cast.getTypeRef());
        var callAdd = (AstCall) cast.getExpression();
        assertEquals(builtinEntityRef("+"), callAdd.getFunction());
        assertEquals(2, callAdd.getArgs().size());
        assertEquals(AstNumberLiteral.of(1), callAdd.getArgs().get(0));
        assertEquals(AstNumberLiteral.of(2), callAdd.getArgs().get(1));
      }
    }
    {
      var expr = ((AstLocal) statements.get(i++)).getVariable().getExpression();
      {
        var call = (AstCall) expr;
        assertEquals(builtinEntityRef("*"), call.getFunction());
        {
          var fieldAccess = (AstFieldAccess) call.getArgs().get(0);
          assertEquals(AstEntityRef.ofName("p"), fieldAccess.getObject());
          assertEquals("t", fieldAccess.getFieldName());
        }
        assertEquals(AstNumberLiteral.of(5), call.getArgs().get(1));
      }
    }
  }

  @Test
  void testInvokeExpression() {
    var code =
        """
        module test;
        function f() {
          var x = foo();
          # Allowed, because we case with different expression:
          var x = (foo)();
          var x = foo(1, 2);
        }
        """;
    var module = parseCode(code, makeErrorConsumer());
    var statements = functions(module).get(0).getBlock().statements();
    assertEquals(3, statements.size());
    int i = 0;
    {
      var expr = ((AstLocal) statements.get(i++)).getVariable().getExpression();
      var call = (AstCall) expr;
      assertEquals(AstEntityRef.ofName("foo"), call.getFunction());
      assertEquals(ImmutableList.of(), call.getArgs());
    }
    {
      var expr = ((AstLocal) statements.get(i++)).getVariable().getExpression();
      var call = (AstCall) expr;
      assertEquals(AstEntityRef.ofName("foo"), call.getFunction());
      assertEquals(ImmutableList.of(), call.getArgs());
    }
    {
      var expr = ((AstLocal) statements.get(i++)).getVariable().getExpression();
      var call = (AstCall) expr;
      assertEquals(AstEntityRef.ofName("foo"), call.getFunction());
      assertEquals(2, call.getArgs().size());
      assertEquals(AstNumberLiteral.of(1), call.getArgs().get(0));
      assertEquals(AstNumberLiteral.of(2), call.getArgs().get(1));
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
    var statements = functions(module).get(0).getBlock().statements();
    assertEquals(4, statements.size());
    int i = 0;
    {
      var expr = ((AstLocal) statements.get(i++)).getVariable().getExpression();
      var field = (AstFieldAccess) expr;
      assertEquals(AstEntityRef.ofName("foo"), field.getObject());
      assertEquals("bar", field.getFieldName());
    }
    {
      var expr = ((AstLocal) statements.get(i++)).getVariable().getExpression();
      var bind = (AstBind) expr;
      assertEquals(AstEntityRef.ofName("foo"), bind.getObject());
      assertEquals("bar", bind.getFunctionName());
    }
    {
      var expr = ((AstLocal) statements.get(i++)).getVariable().getExpression();
      var index = (AstArrayAccess) expr;
      assertEquals(AstEntityRef.ofName("foo"), index.getArray());
      assertEquals(AstNumberLiteral.of(1), index.getIndex());
    }
    {
      var expr = ((AstLocal) statements.get(i++)).getVariable().getExpression();
      var bind = (AstBind) expr;
      assertEquals("baz", bind.getFunctionName());
      {
        var index = (AstArrayAccess) bind.getObject();
        assertEquals(AstNumberLiteral.of(3), index.getIndex());
        {
          var call = (AstCall) index.getArray();
          {
            var field = (AstFieldAccess) call.getFunction();
            assertEquals(AstEntityRef.ofName("foo"), field.getObject());
            assertEquals("bar", field.getFieldName());
          }
          assertEquals(2, call.getArgs().size());
          assertEquals(AstNumberLiteral.of(1), call.getArgs().get(0));
          assertEquals(AstNumberLiteral.of(2), call.getArgs().get(1));
        }
      }
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
    var statements = functions(module).get(0).getBlock().statements();
    assertEquals(3, statements.size());
    int i = 0;
    {
      var stmt = (AstIf) statements.get(i++);
      assertEquals(1, stmt.getConditions().size());
      assertEquals(1, stmt.getThenBlocks().size());
      assertEquals(AstEntityRef.ofName("a"), stmt.getConditions().get(0));
      assertNull(stmt.getElseBlock());
    }
    {
      var stmt = (AstIf) statements.get(i++);
      assertEquals(1, stmt.getConditions().size());
      assertEquals(1, stmt.getThenBlocks().size());
      assertEquals(AstEntityRef.ofName("a"), stmt.getConditions().get(0));
      assertNotNull(stmt.getElseBlock());
    }
    {
      var stmt = (AstIf) statements.get(i++);
      assertEquals(2, stmt.getConditions().size());
      assertEquals(2, stmt.getThenBlocks().size());
      assertEquals(AstEntityRef.ofName("a"), stmt.getConditions().get(0));
      assertEquals(AstEntityRef.ofName("b"), stmt.getConditions().get(1));
      assertNotNull(stmt.getElseBlock());
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
    var statements = functions(module).get(0).getBlock().statements();
    assertEquals(3, statements.size());
    int i = 0;
    {
      var stmt = (AstFor) statements.get(i++);
      assertNull(stmt.getInit());
      assertEquals(AstEntityRef.ofName("true"), stmt.getCondition());
      assertNull(stmt.getIncrement());
      var bodyStatements = stmt.getBlock().statements();
      assertEquals(1, bodyStatements.size());
      assertEquals(new AstGoto("end"), bodyStatements.get(0));
    }
    {
      var stmt = (AstFor) statements.get(i++);
      var init = stmt.getInit();
      assertEquals(new QualifiedName("i"), init.getName());
      assertNull(init.getType());
      assertEquals(AstNumberLiteral.of(0), init.getExpression());
      assertEquals(AstEntityRef.ofName("true"), stmt.getCondition());
      assertNull(stmt.getIncrement());
      assertEquals(ImmutableList.of(), stmt.getBlock().statements());
    }
    {
      var stmt = (AstFor) statements.get(i++);
      var init = stmt.getInit();
      assertEquals(new QualifiedName("i"), init.getName());
      assertNull(init.getType());
      assertEquals(AstNumberLiteral.of(0), init.getExpression());
      assertEquals(AstEntityRef.ofName("true"), stmt.getCondition());
      assertEquals(AstEntityRef.ofName("i"), stmt.getIncrement());
      assertEquals(ImmutableList.of(), stmt.getBlock().statements());
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
    var statements = functions(module).get(0).getBlock().statements();
    assertEquals(1, statements.size());
    {
      var stmt = (AstReturn) statements.get(0);
      assertEquals(AstEntityRef.ofName("value"), stmt.getExpression());
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
    var statements = functions(module).get(0).getBlock().statements();
    assertEquals(2, statements.size());
    {
      var stmt = (AstDefer) statements.get(0);
      assertEquals(1, stmt.getBlock().statements().size());
      var exprStmt = (AstExpressionStatement) stmt.getBlock().statements().get(0);
      assertEquals(AstEntityRef.ofName("value"), exprStmt.getExpression());
    }
    {
      var stmt = (AstDefer) statements.get(1);
      assertEquals(1, stmt.getBlock().statements().size());
      var exprStmt = (AstExpressionStatement) stmt.getBlock().statements().get(0);
      assertEquals(AstEntityRef.ofName("value"), exprStmt.getExpression());
    }
  }
}
