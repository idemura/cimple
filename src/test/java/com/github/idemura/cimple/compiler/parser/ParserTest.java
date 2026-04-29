package com.github.idemura.cimple.compiler.parser;

import static org.junit.jupiter.api.Assertions.*;

import com.github.idemura.cimple.compiler.CompilerException;
import com.github.idemura.cimple.compiler.ast.AstDefer;
import com.github.idemura.cimple.compiler.ast.AstFor;
import com.github.idemura.cimple.compiler.ast.AstGoto;
import com.github.idemura.cimple.compiler.ast.AstIf;
import com.github.idemura.cimple.compiler.ast.AstLiteral;
import com.github.idemura.cimple.compiler.ast.AstModule;
import com.github.idemura.cimple.compiler.ast.AstNameRef;
import com.github.idemura.cimple.compiler.ast.AstTypeAlias;
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
    assertEquals(3, variables.size());
    {
      var v = variables.get(0);
      assertEquals(new QualifiedName("v0"), v.getName());
      assertEquals(TypeRef.ofName("int"), v.getTypeRef());
    }
    {
      var v = variables.get(1);
      assertEquals(new QualifiedName("v1"), v.getName());
      assertEquals(TypeRef.ofName("int"), v.getTypeRef());
    }
    {
      var v = variables.get(2);
      assertEquals(new QualifiedName("v2"), v.getName());
      assertNull(v.getTypeRef());
    }
  }

  @Test
  void testTypes() {
    var code =
        """
        module test;

        type struct Empty {}

        type struct Point {
          var x int;
          var y int;
          const name string;
        }

        type alias Uri = string;

        type union Option {
          None;
          Some(string);
        }

        type function Compare(a int, b int) bool;
        type function Supplier() string;
        type function Consumer(v string);
        """;
    var module = parseCode(code);
    assertEquals("test", module.getName());
    var types = module.types();
    assertEquals(7, types.size());
    int i = 0;
    {
      var type = (AstTypeStruct) types.get(i++);
      assertEquals(new QualifiedName("Empty"), type.getName());
      assertEquals(ImmutableList.of(), type.getFields());
    }
    {
      var type = (AstTypeStruct) types.get(i++);
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
    {
      var type = (AstTypeAlias) types.get(i++);
      assertEquals(new QualifiedName("Uri"), type.getName());
      assertEquals(TypeRef.ofName("string"), type.getBaseTypeRef());
    }
    {
      var type = (AstTypeUnion) types.get(i++);
      assertEquals(new QualifiedName("Option"), type.getName());
      assertEquals(
          ImmutableList.of(unionVariant("None", null), unionVariant("Some", "string")),
          type.getVariants());
    }
    {
      var type = (AstTypeFunction) types.get(i++);
      assertEquals(new QualifiedName("Compare"), type.getName());
      assertEquals(TypeRef.ofName("bool"), type.getHeader().getResultType());
      var params = type.getHeader().getParameters();
      assertEquals(2, params.size());
      assertEquals(parameter("a", "int"), params.get(0));
      assertEquals(parameter("b", "int"), params.get(1));
    }
    {
      var type = (AstTypeFunction) types.get(i++);
      assertEquals(new QualifiedName("Supplier"), type.getName());
      assertEquals(TypeRef.ofName("string"), type.getHeader().getResultType());
      assertEquals(ImmutableList.of(), type.getHeader().getParameters());
    }
    {
      var type = (AstTypeFunction) types.get(i++);
      assertEquals(new QualifiedName("Consumer"), type.getName());
      assertNull(type.getHeader().getResultType());
      assertEquals(ImmutableList.of(parameter("v", "string")), type.getHeader().getParameters());
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
    var function = module.functions().get(0);
    assertEquals(new QualifiedName("f"), function.getHeader().getName());
    var statements = function.getBlock().statements();
    assertEquals(3, statements.size());
    int i = 0;
    {
      var stmtIf = (AstIf) statements.get(i++);
      assertEquals(1, stmtIf.getConditions().size());
      assertEquals(1, stmtIf.getThenBlocks().size());
      assertEquals(new AstNameRef("a"), stmtIf.getConditions().get(0));
      assertNull(stmtIf.getElseBlock());
    }
    {
      var stmtIf = (AstIf) statements.get(i++);
      assertEquals(1, stmtIf.getConditions().size());
      assertEquals(1, stmtIf.getThenBlocks().size());
      assertEquals(new AstNameRef("a"), stmtIf.getConditions().get(0));
      assertNotNull(stmtIf.getElseBlock());
    }
    {
      var stmtIf = (AstIf) statements.get(i++);
      assertEquals(2, stmtIf.getConditions().size());
      assertEquals(2, stmtIf.getThenBlocks().size());
      assertEquals(new AstNameRef("a"), stmtIf.getConditions().get(0));
      assertEquals(new AstNameRef("b"), stmtIf.getConditions().get(1));
      assertNotNull(stmtIf.getElseBlock());
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
    var function = module.functions().get(0);
    assertEquals(new QualifiedName("f"), function.getHeader().getName());
    var statements = function.getBlock().statements();
    assertEquals(3, statements.size());
    int i = 0;
    {
      var stmtFor = (AstFor) statements.get(i++);
      assertNull(stmtFor.getInit());
      assertEquals(new AstNameRef("true"), stmtFor.getCondition());
      assertNull(stmtFor.getIncrement());
      var bodyStatements = stmtFor.getBlock().statements();
      assertEquals(1, bodyStatements.size());
      assertEquals(new AstGoto("end"), bodyStatements.get(0));
    }
    {
      var stmtFor = (AstFor) statements.get(i++);
      var init = stmtFor.getInit();
      assertEquals(new QualifiedName("i"), init.getName());
      assertNull(init.getTypeRef());
      assertEquals(AstLiteral.ofInt(0), init.getExpression());
      assertEquals(new AstNameRef("true"), stmtFor.getCondition());
      assertNull(stmtFor.getIncrement());
      assertEquals(ImmutableList.of(), stmtFor.getBlock().statements());
    }
    {
      var stmtFor = (AstFor) statements.get(i++);
      var init = stmtFor.getInit();
      assertEquals(new QualifiedName("i"), init.getName());
      assertNull(init.getTypeRef());
      assertEquals(AstLiteral.ofInt(0), init.getExpression());
      assertEquals(new AstNameRef("true"), stmtFor.getCondition());
      assertEquals(new AstNameRef("i"), stmtFor.getIncrement());
      assertEquals(ImmutableList.of(), stmtFor.getBlock().statements());
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
  void testDeferStatement() {
    var code =
        """
        module test;
        function f() {
          defer value;
        }
        """;
    var module = parseCode(code);
    var function = module.functions().get(0);
    assertEquals(new QualifiedName("f"), function.getHeader().getName());
    var statements = function.getBlock().statements();
    {
      var stmtDefer = (AstDefer) statements.get(0);
      assertEquals(new AstNameRef("value"), stmtDefer.getExpression());
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
