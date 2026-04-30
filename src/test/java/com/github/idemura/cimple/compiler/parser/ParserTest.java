package com.github.idemura.cimple.compiler.parser;

import static org.junit.jupiter.api.Assertions.*;

import com.github.idemura.cimple.compiler.CompilerException;
import com.github.idemura.cimple.compiler.ast.AstApplyFunction;
import com.github.idemura.cimple.compiler.ast.AstDefer;
import com.github.idemura.cimple.compiler.ast.AstFor;
import com.github.idemura.cimple.compiler.ast.AstGoto;
import com.github.idemura.cimple.compiler.ast.AstIf;
import com.github.idemura.cimple.compiler.ast.AstLiteral;
import com.github.idemura.cimple.compiler.ast.AstModule;
import com.github.idemura.cimple.compiler.ast.AstNameRef;
import com.github.idemura.cimple.compiler.ast.AstReturn;
import com.github.idemura.cimple.compiler.ast.AstExpressionStatement;
import com.github.idemura.cimple.compiler.ast.AstTypeAlias;
import com.github.idemura.cimple.compiler.ast.AstTypeCast;
import com.github.idemura.cimple.compiler.ast.AstTypeFunction;
import com.github.idemura.cimple.compiler.ast.AstTypeStruct;
import com.github.idemura.cimple.compiler.ast.AstTypeUnion;
import com.github.idemura.cimple.compiler.ast.AstVariable;
import com.github.idemura.cimple.compiler.ast.QualifiedName;
import com.github.idemura.cimple.compiler.ast.TypeRef;
import com.github.idemura.cimple.compiler.ast.UnionVariant;
import com.github.idemura.cimple.compiler.tokens.Tokenizer;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

class ParserTest {
  private AstModule parseCode(String code) {
    return new Parser(new Tokenizer(code).split()).parse();
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
    var functions = module.functions();
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
      assertEquals(TypeRef.ofName("int"), f.getHeader().getResultType());
      assertEquals(ImmutableList.of(), f.getHeader().getParameters());
    }
    var variables = module.variables();
    assertEquals(4, variables.size());
    {
      var v = variables.get(0);
      assertEquals(new QualifiedName("v0"), v.getName());
      assertEquals(TypeRef.ofName("int"), v.getTypeRef());
      assertTrue(v.getBit(AstVariable.MUTABLE));
    }
    {
      var v = variables.get(1);
      assertEquals(new QualifiedName("v1"), v.getName());
      assertEquals(TypeRef.ofName("int"), v.getTypeRef());
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
      assertEquals(TypeRef.ofName("int"), v.getTypeRef());
      assertFalse(v.getBit(AstVariable.MUTABLE));
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
    assertEquals("test", module.getName());
    var types = module.types();
    assertEquals(2, types.size());
    {
      var type = (AstTypeStruct) types.get(0);
      assertEquals(new QualifiedName("Empty"), type.getName());
      assertEquals(ImmutableList.of(), type.getFields());
    }
    {
      var type = (AstTypeStruct) types.get(1);
      assertEquals(new QualifiedName("Point"), type.getName());
      var fields = type.getFields();
      assertEquals(3, fields.size());
      int j = 0;
      {
        var f = fields.get(j++);
        assertEquals(new QualifiedName("x"), f.getName());
        assertEquals(TypeRef.ofName("int"), f.getTypeRef());
        assertTrue(f.getBit(AstVariable.MUTABLE));
      }
      {
        var f = fields.get(j++);
        assertEquals(new QualifiedName("y"), f.getName());
        assertEquals(TypeRef.ofName("int"), f.getTypeRef());
        assertTrue(f.getBit(AstVariable.MUTABLE));
      }
      {
        var f = fields.get(j++);
        assertEquals(new QualifiedName("name"), f.getName());
        assertEquals(TypeRef.ofName("string"), f.getTypeRef());
        assertFalse(f.getBit(AstVariable.MUTABLE));
      }
    }
  }

  @Test
  void testAliasType() {
    var code =
        """
        module test;
        type alias Uri = string;
        """;
    var module = parseCode(code);
    assertEquals("test", module.getName());
    var types = module.types();
    assertEquals(1, types.size());
    var type = (AstTypeAlias) types.get(0);
    assertEquals(new QualifiedName("Uri"), type.getName());
    assertEquals(TypeRef.ofName("string"), type.getBaseTypeRef());
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
    var types = module.types();
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
    var types = module.types();
    assertEquals(3, types.size());
    {
      var type = (AstTypeFunction) types.get(0);
      assertEquals(new QualifiedName("Compare"), type.getName());
      assertEquals(TypeRef.ofName("bool"), type.getHeader().getResultType());
      var params = type.getHeader().getParameters();
      assertEquals(2, params.size());
      assertEquals(parameter("a", "int"), params.get(0));
      assertEquals(parameter("b", "int"), params.get(1));
    }
    {
      var type = (AstTypeFunction) types.get(1);
      assertEquals(new QualifiedName("Supplier"), type.getName());
      assertEquals(TypeRef.ofName("string"), type.getHeader().getResultType());
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
    var statements = module.functions().get(0).getBlock().statements();
    assertEquals(9, statements.size());
    int i = 0;
    {
      var expr = ((AstVariable) statements.get(i++)).getExpression();
      assertEquals(AstLiteral.ofInt(1), expr);
    }
    {
      var expr = ((AstVariable) statements.get(i++)).getExpression();
      {
        var apply = (AstApplyFunction) expr;
        assertEquals(new AstNameRef("+"), apply.getFunction());
        assertEquals(2, apply.getArgs().size());
        assertEquals(AstLiteral.ofInt(1), apply.getArgs().get(0));
        assertEquals(AstLiteral.ofInt(2), apply.getArgs().get(1));
      }
    }
    {
      var expr = ((AstVariable) statements.get(i++)).getExpression();
      {
        var applySub = (AstApplyFunction) expr;
        assertEquals(new AstNameRef("-"), applySub.getFunction());
        assertEquals(2, applySub.getArgs().size());
        {
          var applyAdd = (AstApplyFunction) applySub.getArgs().get(0);
          assertEquals(new AstNameRef("+"), applyAdd.getFunction());
          assertEquals(2, applyAdd.getArgs().size());
          assertEquals(AstLiteral.ofInt(1), applyAdd.getArgs().get(0));
          assertEquals(AstLiteral.ofInt(2), applyAdd.getArgs().get(1));
        }
        assertEquals(AstLiteral.ofInt(3), applySub.getArgs().get(1));
      }
    }
    {
      var expr = ((AstVariable) statements.get(i++)).getExpression();
      {
        var applyMul = (AstApplyFunction) expr;
        assertEquals(new AstNameRef("*"), applyMul.getFunction());
        assertEquals(2, applyMul.getArgs().size());
        assertEquals(AstLiteral.ofInt(1), applyMul.getArgs().get(0));
        assertEquals(AstLiteral.ofInt(2), applyMul.getArgs().get(1));
      }
    }
    {
      var expr = ((AstVariable) statements.get(i++)).getExpression();
      {
        var applyMul = (AstApplyFunction) expr;
        assertEquals(new AstNameRef("*"), applyMul.getFunction());
        assertEquals(2, applyMul.getArgs().size());
        {
          var nesteApplyMul = (AstApplyFunction) applyMul.getArgs().get(0);
          assertEquals(new AstNameRef("*"), nesteApplyMul.getFunction());
          assertEquals(2, nesteApplyMul.getArgs().size());
          assertEquals(AstLiteral.ofInt(1), nesteApplyMul.getArgs().get(0));
          assertEquals(AstLiteral.ofInt(2), nesteApplyMul.getArgs().get(1));
        }
        assertEquals(AstLiteral.ofInt(3), applyMul.getArgs().get(1));
      }
    }
    {
      var expr = ((AstVariable) statements.get(i++)).getExpression();
      {
        var applyAdd = (AstApplyFunction) expr;
        assertEquals(new AstNameRef("+"), applyAdd.getFunction());
        assertEquals(2, applyAdd.getArgs().size());
        assertEquals(AstLiteral.ofInt(1), applyAdd.getArgs().get(0));
        {
          var applyMul = (AstApplyFunction) applyAdd.getArgs().get(1);
          assertEquals(new AstNameRef("*"), applyMul.getFunction());
          assertEquals(2, applyMul.getArgs().size());
          assertEquals(AstLiteral.ofInt(2), applyMul.getArgs().get(0));
          assertEquals(AstLiteral.ofInt(3), applyMul.getArgs().get(1));
        }
      }
    }
    {
      var expr = ((AstVariable) statements.get(i++)).getExpression();
      {
        var applyAdd = (AstApplyFunction) expr;
        assertEquals(new AstNameRef("+"), applyAdd.getFunction());
        assertEquals(2, applyAdd.getArgs().size());
        {
          var applyMul = (AstApplyFunction) applyAdd.getArgs().get(0);
          assertEquals(new AstNameRef("*"), applyMul.getFunction());
          assertEquals(2, applyMul.getArgs().size());
          assertEquals(AstLiteral.ofInt(1), applyMul.getArgs().get(0));
          assertEquals(AstLiteral.ofInt(2), applyMul.getArgs().get(1));
        }
        assertEquals(AstLiteral.ofInt(3), applyAdd.getArgs().get(1));
      }
    }
    {
      var expr = ((AstVariable) statements.get(i++)).getExpression();
      {
        var apply = (AstApplyFunction) expr;
        assertEquals(new AstNameRef("+"), apply.getFunction());
        assertEquals(2, apply.getArgs().size());
        assertEquals(AstLiteral.ofInt(1), apply.getArgs().get(0));
        assertEquals(AstLiteral.ofInt(2), apply.getArgs().get(1));
      }
    }
    {
      var expr = ((AstVariable) statements.get(i++)).getExpression();
      {
        var cast = (AstTypeCast) expr;
        assertEquals(TypeRef.ofName("int"), cast.getTypeRef());
        var applyAdd = (AstApplyFunction) cast.getExpression();
        assertEquals(new AstNameRef("+"), applyAdd.getFunction());
        assertEquals(2, applyAdd.getArgs().size());
        assertEquals(AstLiteral.ofInt(1), applyAdd.getArgs().get(0));
        assertEquals(AstLiteral.ofInt(2), applyAdd.getArgs().get(1));
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
    var statements = module.functions().get(0).getBlock().statements();
    assertEquals(3, statements.size());
    int i = 0;
    {
      var expr = ((AstVariable) statements.get(i++)).getExpression();
      var apply = (AstApplyFunction) expr;
      assertEquals(new AstNameRef("foo"), apply.getFunction());
      assertEquals(ImmutableList.of(), apply.getArgs());
    }
    {
      var expr = ((AstVariable) statements.get(i++)).getExpression();
      var apply = (AstApplyFunction) expr;
      assertEquals(new AstNameRef("foo"), apply.getFunction());
      assertEquals(ImmutableList.of(), apply.getArgs());
    }
    {
      var expr = ((AstVariable) statements.get(i++)).getExpression();
      var apply = (AstApplyFunction) expr;
      assertEquals(new AstNameRef("foo"), apply.getFunction());
      assertEquals(2, apply.getArgs().size());
      assertEquals(AstLiteral.ofInt(1), apply.getArgs().get(0));
      assertEquals(AstLiteral.ofInt(2), apply.getArgs().get(1));
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
    var statements = module.functions().get(0).getBlock().statements();
    assertEquals(3, statements.size());
    int i = 0;
    {
      var stmt = (AstIf) statements.get(i++);
      assertEquals(1, stmt.getConditions().size());
      assertEquals(1, stmt.getThenBlocks().size());
      assertEquals(new AstNameRef("a"), stmt.getConditions().get(0));
      assertNull(stmt.getElseBlock());
    }
    {
      var stmt = (AstIf) statements.get(i++);
      assertEquals(1, stmt.getConditions().size());
      assertEquals(1, stmt.getThenBlocks().size());
      assertEquals(new AstNameRef("a"), stmt.getConditions().get(0));
      assertNotNull(stmt.getElseBlock());
    }
    {
      var stmt = (AstIf) statements.get(i++);
      assertEquals(2, stmt.getConditions().size());
      assertEquals(2, stmt.getThenBlocks().size());
      assertEquals(new AstNameRef("a"), stmt.getConditions().get(0));
      assertEquals(new AstNameRef("b"), stmt.getConditions().get(1));
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
    var statements = module.functions().get(0).getBlock().statements();
    assertEquals(3, statements.size());
    int i = 0;
    {
      var stmt = (AstFor) statements.get(i++);
      assertNull(stmt.getInit());
      assertEquals(new AstNameRef("true"), stmt.getCondition());
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
      assertEquals(AstLiteral.ofInt(0), init.getExpression());
      assertEquals(new AstNameRef("true"), stmt.getCondition());
      assertNull(stmt.getIncrement());
      assertEquals(ImmutableList.of(), stmt.getBlock().statements());
    }
    {
      var stmt = (AstFor) statements.get(i++);
      var init = stmt.getInit();
      assertEquals(new QualifiedName("i"), init.getName());
      assertNull(init.getTypeRef());
      assertEquals(AstLiteral.ofInt(0), init.getExpression());
      assertEquals(new AstNameRef("true"), stmt.getCondition());
      assertEquals(new AstNameRef("i"), stmt.getIncrement());
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
    var statements = module.functions().get(0).getBlock().statements();
    assertEquals(1, statements.size());
    {
      var stmt = (AstReturn) statements.get(0);
      assertEquals(new AstNameRef("value"), stmt.getExpression());
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
    var statements = module.functions().get(0).getBlock().statements();
    assertEquals(2, statements.size());
    {
      var stmt = (AstDefer) statements.get(0);
      assertEquals(1, stmt.getBlock().statements().size());
      var exprStmt = (AstExpressionStatement) stmt.getBlock().statements().get(0);
      assertEquals(new AstNameRef("value"), exprStmt.getExpression());
    }
    {
      var stmt = (AstDefer) statements.get(1);
      assertEquals(1, stmt.getBlock().statements().size());
      var exprStmt = (AstExpressionStatement) stmt.getBlock().statements().get(0);
      assertEquals(new AstNameRef("value"), exprStmt.getExpression());
    }
  }

  private static AstVariable parameter(String name, String typeName) {
    var parameter = new AstVariable();
    parameter.setName(name);
    parameter.setTypeRef(TypeRef.ofName(typeName));
    parameter.setBit(AstVariable.PARAM);
    return parameter;
  }

  private static UnionVariant unionVariant(String name, String typeName) {
    var unionVariant = new UnionVariant();
    unionVariant.setName(name);
    if (typeName != null) {
      unionVariant.setValueType(TypeRef.ofName(typeName));
    }
    return unionVariant;
  }
}
