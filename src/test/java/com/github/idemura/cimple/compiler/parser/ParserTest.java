package com.github.idemura.cimple.compiler.parser;

import static org.junit.jupiter.api.Assertions.*;

import com.github.idemura.cimple.compiler.CompilerException;
import com.github.idemura.cimple.compiler.ast.AstArrayAccess;
import com.github.idemura.cimple.compiler.ast.AstBind;
import com.github.idemura.cimple.compiler.ast.AstCall;
import com.github.idemura.cimple.compiler.ast.AstCast;
import com.github.idemura.cimple.compiler.ast.AstDefer;
import com.github.idemura.cimple.compiler.ast.AstExpressionStatement;
import com.github.idemura.cimple.compiler.ast.AstFieldAccess;
import com.github.idemura.cimple.compiler.ast.AstFor;
import com.github.idemura.cimple.compiler.ast.AstFunction;
import com.github.idemura.cimple.compiler.ast.AstGoto;
import com.github.idemura.cimple.compiler.ast.AstIf;
import com.github.idemura.cimple.compiler.ast.AstModule;
import com.github.idemura.cimple.compiler.ast.AstName;
import com.github.idemura.cimple.compiler.ast.AstNumberLiteral;
import com.github.idemura.cimple.compiler.ast.AstReturn;
import com.github.idemura.cimple.compiler.ast.AstType;
import com.github.idemura.cimple.compiler.ast.AstTypeAlias;
import com.github.idemura.cimple.compiler.ast.AstTypeFunction;
import com.github.idemura.cimple.compiler.ast.AstTypeRecord;
import com.github.idemura.cimple.compiler.ast.AstTypeUnion;
import com.github.idemura.cimple.compiler.ast.AstVariable;
import com.github.idemura.cimple.compiler.ast.QualifiedName;
import com.github.idemura.cimple.compiler.ast.TypeRef;
import com.github.idemura.cimple.compiler.ast.UnionVariant;
import com.github.idemura.cimple.compiler.tokens.Tokenizer;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ParserTest {
  private AstModule parseCode(String code) {
    return new Parser(new Tokenizer(code).split()).parse();
  }

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
    var module = parseCode(code);
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
      assertEquals(TypeRef.of("int"), f.getHeader().getResultType());
      assertEquals(ImmutableList.of(), f.getHeader().getParameters());
    }
    var variables = variables(module);
    assertEquals(4, variables.size());
    {
      var v = variables.get(0);
      assertEquals(new QualifiedName("v0"), v.getName());
      assertEquals(TypeRef.of("int"), v.getTypeRef());
      assertTrue(v.getBit(AstVariable.MUTABLE));
    }
    {
      var v = variables.get(1);
      assertEquals(new QualifiedName("v1"), v.getName());
      assertEquals(TypeRef.of("int"), v.getTypeRef());
      assertTrue(v.getBit(AstVariable.MUTABLE));
    }
    {
      var v = variables.get(2);
      assertEquals(new QualifiedName("v2"), v.getName());
      assertNull(v.getTypeRef());
      assertTrue(v.getBit(AstVariable.MUTABLE));
    }
    {
      var v = variables.get(3);
      assertEquals(new QualifiedName("c0"), v.getName());
      assertEquals(TypeRef.of("int"), v.getTypeRef());
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
    var module = parseCode(code);
    assertEquals("test", module.getName());
    var types = types(module);
    assertEquals(2, types.size());
    {
      var type = (AstTypeRecord) types.get(0);
      assertEquals(new QualifiedName("Empty"), type.getName());
      assertEquals(ImmutableList.of(), type.getFields());
    }
    {
      var type = (AstTypeRecord) types.get(1);
      assertEquals(new QualifiedName("Point"), type.getName());
      var fields = type.getFields();
      assertEquals(3, fields.size());
      int j = 0;
      {
        var f = fields.get(j++);
        assertEquals(new QualifiedName("x"), f.getName());
        assertEquals(TypeRef.of("int"), f.getTypeRef());
        assertTrue(f.getBit(AstVariable.MUTABLE));
      }
      {
        var f = fields.get(j++);
        assertEquals(new QualifiedName("y"), f.getName());
        assertEquals(TypeRef.of("int"), f.getTypeRef());
        assertTrue(f.getBit(AstVariable.MUTABLE));
      }
      {
        var f = fields.get(j++);
        assertEquals(new QualifiedName("name"), f.getName());
        assertEquals(TypeRef.of("string"), f.getTypeRef());
        assertFalse(f.getBit(AstVariable.MUTABLE));
      }
    }
  }

  @Test
  void testOpaqueType() {
    var code =
        """
        module test;
        type opaque Uri string;
        """;
    var module = parseCode(code);
    assertEquals("test", module.getName());
    var types = types(module);
    assertEquals(1, types.size());
    var type = (AstTypeAlias) types.get(0);
    assertEquals(new QualifiedName("Uri"), type.getName());
    assertEquals(TypeRef.of("string"), type.getBaseTypeRef());
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
    var module = parseCode(code);
    assertEquals("test", module.getName());
    var types = types(module);
    assertEquals(1, types.size());
    var type = (AstTypeUnion) types.get(0);
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
    var module = parseCode(code);
    assertEquals("test", module.getName());
    var types = types(module);
    assertEquals(3, types.size());
    {
      var type = (AstTypeFunction) types.get(0);
      assertEquals(new QualifiedName("Compare"), type.getName());
      assertEquals(TypeRef.of("bool"), type.getHeader().getResultType());
      var params = type.getHeader().getParameters();
      assertEquals(2, params.size());
      assertEquals(parameter("a", "int"), params.get(0));
      assertEquals(parameter("b", "int"), params.get(1));
    }
    {
      var type = (AstTypeFunction) types.get(1);
      assertEquals(new QualifiedName("Supplier"), type.getName());
      assertEquals(TypeRef.of("string"), type.getHeader().getResultType());
      assertEquals(ImmutableList.of(), type.getHeader().getParameters());
    }
    {
      var type = (AstTypeFunction) types.get(2);
      assertEquals(new QualifiedName("Consumer"), type.getName());
      assertNull(type.getHeader().getResultType());
      assertEquals(ImmutableList.of(parameter("v", "string")), type.getHeader().getParameters());
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
          var x = 1 * 2 * 3;
          var x = 1 + 2 * 3;
          var x = 1 * 2 + 3;
          var x = (1 + 2);
          var x = [1 + 2 type int];
        }
        """;
    var module = parseCode(code);
    var statements = functions(module).get(0).getBlock().statements();
    assertEquals(9, statements.size());
    int i = 0;
    {
      var expr = ((AstVariable) statements.get(i++)).getExpression();
      assertEquals(AstNumberLiteral.of(1), expr);
    }
    {
      var expr = ((AstVariable) statements.get(i++)).getExpression();
      {
        var call = (AstCall) expr;
        assertEquals(new AstName("+"), call.getFunction());
        assertEquals(2, call.getArgs().size());
        assertEquals(AstNumberLiteral.of(1), call.getArgs().get(0));
        assertEquals(AstNumberLiteral.of(2), call.getArgs().get(1));
      }
    }
    {
      var expr = ((AstVariable) statements.get(i++)).getExpression();
      {
        var callSub = (AstCall) expr;
        assertEquals(new AstName("-"), callSub.getFunction());
        assertEquals(2, callSub.getArgs().size());
        {
          var callAdd = (AstCall) callSub.getArgs().get(0);
          assertEquals(new AstName("+"), callAdd.getFunction());
          assertEquals(2, callAdd.getArgs().size());
          assertEquals(AstNumberLiteral.of(1), callAdd.getArgs().get(0));
          assertEquals(AstNumberLiteral.of(2), callAdd.getArgs().get(1));
        }
        assertEquals(AstNumberLiteral.of(3), callSub.getArgs().get(1));
      }
    }
    {
      var expr = ((AstVariable) statements.get(i++)).getExpression();
      {
        var callMul = (AstCall) expr;
        assertEquals(new AstName("*"), callMul.getFunction());
        assertEquals(2, callMul.getArgs().size());
        assertEquals(AstNumberLiteral.of(1), callMul.getArgs().get(0));
        assertEquals(AstNumberLiteral.of(2), callMul.getArgs().get(1));
      }
    }
    {
      var expr = ((AstVariable) statements.get(i++)).getExpression();
      {
        var callMul = (AstCall) expr;
        assertEquals(new AstName("*"), callMul.getFunction());
        assertEquals(2, callMul.getArgs().size());
        {
          var nestedCallMul = (AstCall) callMul.getArgs().get(0);
          assertEquals(new AstName("*"), nestedCallMul.getFunction());
          assertEquals(2, nestedCallMul.getArgs().size());
          assertEquals(AstNumberLiteral.of(1), nestedCallMul.getArgs().get(0));
          assertEquals(AstNumberLiteral.of(2), nestedCallMul.getArgs().get(1));
        }
        assertEquals(AstNumberLiteral.of(3), callMul.getArgs().get(1));
      }
    }
    {
      var expr = ((AstVariable) statements.get(i++)).getExpression();
      {
        var callAdd = (AstCall) expr;
        assertEquals(new AstName("+"), callAdd.getFunction());
        assertEquals(2, callAdd.getArgs().size());
        assertEquals(AstNumberLiteral.of(1), callAdd.getArgs().get(0));
        {
          var callMul = (AstCall) callAdd.getArgs().get(1);
          assertEquals(new AstName("*"), callMul.getFunction());
          assertEquals(2, callMul.getArgs().size());
          assertEquals(AstNumberLiteral.of(2), callMul.getArgs().get(0));
          assertEquals(AstNumberLiteral.of(3), callMul.getArgs().get(1));
        }
      }
    }
    {
      var expr = ((AstVariable) statements.get(i++)).getExpression();
      {
        var callAdd = (AstCall) expr;
        assertEquals(new AstName("+"), callAdd.getFunction());
        assertEquals(2, callAdd.getArgs().size());
        {
          var callMul = (AstCall) callAdd.getArgs().get(0);
          assertEquals(new AstName("*"), callMul.getFunction());
          assertEquals(2, callMul.getArgs().size());
          assertEquals(AstNumberLiteral.of(1), callMul.getArgs().get(0));
          assertEquals(AstNumberLiteral.of(2), callMul.getArgs().get(1));
        }
        assertEquals(AstNumberLiteral.of(3), callAdd.getArgs().get(1));
      }
    }
    {
      var expr = ((AstVariable) statements.get(i++)).getExpression();
      {
        var call = (AstCall) expr;
        assertEquals(new AstName("+"), call.getFunction());
        assertEquals(2, call.getArgs().size());
        assertEquals(AstNumberLiteral.of(1), call.getArgs().get(0));
        assertEquals(AstNumberLiteral.of(2), call.getArgs().get(1));
      }
    }
    {
      var expr = ((AstVariable) statements.get(i++)).getExpression();
      {
        var cast = (AstCast) expr;
        assertEquals(TypeRef.of("int"), cast.getTypeRef());
        var callAdd = (AstCall) cast.getExpression();
        assertEquals(new AstName("+"), callAdd.getFunction());
        assertEquals(2, callAdd.getArgs().size());
        assertEquals(AstNumberLiteral.of(1), callAdd.getArgs().get(0));
        assertEquals(AstNumberLiteral.of(2), callAdd.getArgs().get(1));
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
    var module = parseCode(code);
    var statements = functions(module).get(0).getBlock().statements();
    assertEquals(3, statements.size());
    int i = 0;
    {
      var expr = ((AstVariable) statements.get(i++)).getExpression();
      var call = (AstCall) expr;
      assertEquals(new AstName("foo"), call.getFunction());
      assertEquals(ImmutableList.of(), call.getArgs());
    }
    {
      var expr = ((AstVariable) statements.get(i++)).getExpression();
      var call = (AstCall) expr;
      assertEquals(new AstName("foo"), call.getFunction());
      assertEquals(ImmutableList.of(), call.getArgs());
    }
    {
      var expr = ((AstVariable) statements.get(i++)).getExpression();
      var call = (AstCall) expr;
      assertEquals(new AstName("foo"), call.getFunction());
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
    var module = parseCode(code);
    var statements = functions(module).get(0).getBlock().statements();
    assertEquals(4, statements.size());
    int i = 0;
    {
      var expr = ((AstVariable) statements.get(i++)).getExpression();
      var field = (AstFieldAccess) expr;
      assertEquals(new AstName("foo"), field.getObject());
      assertEquals("bar", field.getFieldName());
    }
    {
      var expr = ((AstVariable) statements.get(i++)).getExpression();
      var bind = (AstBind) expr;
      assertEquals(new AstName("foo"), bind.getObject());
      assertEquals("bar", bind.getFunctionName());
    }
    {
      var expr = ((AstVariable) statements.get(i++)).getExpression();
      var index = (AstArrayAccess) expr;
      assertEquals(new AstName("foo"), index.getArray());
      assertEquals(AstNumberLiteral.of(1), index.getIndex());
    }
    {
      var expr = ((AstVariable) statements.get(i++)).getExpression();
      var bind = (AstBind) expr;
      assertEquals("baz", bind.getFunctionName());
      {
        var index = (AstArrayAccess) bind.getObject();
        assertEquals(AstNumberLiteral.of(3), index.getIndex());
        {
          var call = (AstCall) index.getArray();
          {
            var field = (AstFieldAccess) call.getFunction();
            assertEquals(new AstName("foo"), field.getObject());
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
    var module = parseCode(code);
    var statements = functions(module).get(0).getBlock().statements();
    assertEquals(3, statements.size());
    int i = 0;
    {
      var stmt = (AstIf) statements.get(i++);
      assertEquals(1, stmt.getConditions().size());
      assertEquals(1, stmt.getThenBlocks().size());
      assertEquals(new AstName("a"), stmt.getConditions().get(0));
      assertNull(stmt.getElseBlock());
    }
    {
      var stmt = (AstIf) statements.get(i++);
      assertEquals(1, stmt.getConditions().size());
      assertEquals(1, stmt.getThenBlocks().size());
      assertEquals(new AstName("a"), stmt.getConditions().get(0));
      assertNotNull(stmt.getElseBlock());
    }
    {
      var stmt = (AstIf) statements.get(i++);
      assertEquals(2, stmt.getConditions().size());
      assertEquals(2, stmt.getThenBlocks().size());
      assertEquals(new AstName("a"), stmt.getConditions().get(0));
      assertEquals(new AstName("b"), stmt.getConditions().get(1));
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
    var module = parseCode(code);
    var statements = functions(module).get(0).getBlock().statements();
    assertEquals(3, statements.size());
    int i = 0;
    {
      var stmt = (AstFor) statements.get(i++);
      assertNull(stmt.getInit());
      assertEquals(new AstName("true"), stmt.getCondition());
      assertNull(stmt.getIncrement());
      var bodyStatements = stmt.getBlock().statements();
      assertEquals(1, bodyStatements.size());
      assertEquals(new AstGoto("end"), bodyStatements.get(0));
    }
    {
      var stmt = (AstFor) statements.get(i++);
      var init = stmt.getInit();
      assertEquals(new QualifiedName("i"), init.getName());
      assertNull(init.getTypeRef());
      assertEquals(AstNumberLiteral.of(0), init.getExpression());
      assertEquals(new AstName("true"), stmt.getCondition());
      assertNull(stmt.getIncrement());
      assertEquals(ImmutableList.of(), stmt.getBlock().statements());
    }
    {
      var stmt = (AstFor) statements.get(i++);
      var init = stmt.getInit();
      assertEquals(new QualifiedName("i"), init.getName());
      assertNull(init.getTypeRef());
      assertEquals(AstNumberLiteral.of(0), init.getExpression());
      assertEquals(new AstName("true"), stmt.getCondition());
      assertEquals(new AstName("i"), stmt.getIncrement());
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
  void testReturnStatement() {
    var code =
        """
        module test;
        function f() {
          return value;
        }
        """;
    var module = parseCode(code);
    var statements = functions(module).get(0).getBlock().statements();
    assertEquals(1, statements.size());
    {
      var stmt = (AstReturn) statements.get(0);
      assertEquals(new AstName("value"), stmt.getExpression());
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
    var statements = functions(module).get(0).getBlock().statements();
    assertEquals(2, statements.size());
    {
      var stmt = (AstDefer) statements.get(0);
      assertEquals(1, stmt.getBlock().statements().size());
      var exprStmt = (AstExpressionStatement) stmt.getBlock().statements().get(0);
      assertEquals(new AstName("value"), exprStmt.getExpression());
    }
    {
      var stmt = (AstDefer) statements.get(1);
      assertEquals(1, stmt.getBlock().statements().size());
      var exprStmt = (AstExpressionStatement) stmt.getBlock().statements().get(0);
      assertEquals(new AstName("value"), exprStmt.getExpression());
    }
  }

  private static AstVariable parameter(String name, String typeName) {
    var parameter = new AstVariable();
    parameter.setName(name);
    parameter.setTypeRef(TypeRef.of(typeName));
    parameter.setBit(AstVariable.PARAM);
    return parameter;
  }

  private static UnionVariant unionVariant(String name, String typeName) {
    var unionVariant = new UnionVariant();
    unionVariant.setName(name);
    if (typeName != null) {
      unionVariant.setValueType(TypeRef.of(typeName));
    }
    return unionVariant;
  }
}
