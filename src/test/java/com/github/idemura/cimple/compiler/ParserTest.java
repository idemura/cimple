package com.github.idemura.cimple.compiler;

import static com.github.idemura.cimple.common.Resources.readResource;
import static com.github.idemura.cimple.compiler.BuiltinTypeRefs.*;
import static com.github.idemura.cimple.compiler.TokenType.NUMBER;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class ParserTest {
  private AstAbstractNode parseCode(String code) {
    return new Parser(new Tokenizer(null, code).split()).parse();
  }

  private AstModule parseFile(String fileName) {
    var code = readResource(getClass(), fileName);
    return (AstModule) parseCode(code);
  }

  @Test
  void testModule() {
    var module = parseFile("/parser/global_defs.ci");
    assertEquals("base_test", module.getName());
    var functions = module.getFunctions();
    assertEquals(4, functions.size());
    {
      var f = functions.get(0);
      assertEquals("f0", f.getName());
      assertNull(f.getResultType());
      assertEquals(List.of(), f.getParameters());
    }
    {
      var f = functions.get(1);
      assertEquals("f1", f.getName());
      assertNull(f.getResultType());
      var params = f.getParameters();
      assertEquals(1, params.size());
      assertEquals(new VariableDef("x", INT), params.get(0));
    }
    {
      var f = functions.get(2);
      assertEquals("f2", f.getName());
      assertNull(f.getResultType());
      var params = f.getParameters();
      assertEquals(2, params.size());
      assertEquals(new VariableDef("x", INT), params.get(0));
      assertEquals(new VariableDef("y", INT), params.get(1));
    }
    {
      var f = functions.get(3);
      assertEquals("r", f.getName());
      assertEquals(INT, f.getResultType());
      assertEquals(List.of(), f.getParameters());
    }
    var variables = module.getVariables();
    assertEquals(3, variables.size());
    {
      var v = variables.get(0);
      assertEquals("v0", v.getName());
      assertEquals(INT, v.getTypeRef());
    }
    {
      var v = variables.get(1);
      assertEquals("v1", v.getName());
      assertEquals(INT, v.getTypeRef());
    }
    {
      var v = variables.get(2);
      assertEquals("v2", v.getName());
      assertNull(v.getTypeRef());
    }
  }

  @Test
  void testTypes() {
    var module = parseFile("/parser/types.ci");
    assertEquals("type_test", module.getName());
    var types = module.getTypes();
    assertEquals(2, types.size());
    {
      var type = (AstTypeStruct) types.get(0);
      assertEquals("Empty", type.getName());
      assertEquals(List.of(), type.getFields());
    }
    {
      var type = (AstTypeStruct) types.get(1);
      assertEquals("Point", type.getName());
      var fields = type.getFields();
      assertEquals(2, fields.size());
      assertEquals("x", fields.get(0).getName());
      assertEquals(INT, fields.get(0).getTypeRef());
      assertEquals("y", fields.get(1).getName());
      assertEquals(INT, fields.get(1).getTypeRef());
    }
  }

  @Test
  void testIfStatement() {
    var module = parseFile("/parser/statements.ci");
    var function = module.getFunctions().get(0);
    assertEquals("f", function.getName());
    var statements = function.getBlock().getStatements();
    assertEquals(3, statements.size());
    {
      var stmtIf = (AstIf) statements.get(0);
      assertEquals(1, stmtIf.getConditions().size());
      assertEquals(1, stmtIf.getThenBlocks().size());
      assertEquals(new AstNameRef("a"), stmtIf.getConditions().get(0));
      assertNull(stmtIf.getElseBlock());
    }
    {
      var stmtIf = (AstIf) statements.get(1);
      assertEquals(1, stmtIf.getConditions().size());
      assertEquals(1, stmtIf.getThenBlocks().size());
      assertEquals(new AstNameRef("a"), stmtIf.getConditions().get(0));
      assertNotNull(stmtIf.getElseBlock());
    }
    {
      var stmtIf = (AstIf) statements.get(2);
      assertEquals(2, stmtIf.getConditions().size());
      assertEquals(2, stmtIf.getThenBlocks().size());
      assertEquals(new AstNameRef("a"), stmtIf.getConditions().get(0));
      assertEquals(new AstNameRef("b"), stmtIf.getConditions().get(1));
      assertNotNull(stmtIf.getElseBlock());
    }
  }

  @Test
  void testForStatement() {
    var module = parseFile("/parser/statements.ci");
    var function = module.getFunctions().get(1);
    assertEquals("g", function.getName());
    var statements = function.getBlock().getStatements();
    assertEquals(3, statements.size());
    {
      var stmtFor = (AstFor) statements.get(0);
      assertNull(stmtFor.getInit());
      assertNull(stmtFor.getCondition());
      var bodyStatements = stmtFor.getBlock().getStatements();
      assertEquals(1, bodyStatements.size());
      assertEquals(new AstGoto("end"), bodyStatements.get(0));
    }
    {
      var stmtFor = (AstFor) statements.get(1);
      var init = stmtFor.getInit();
      assertEquals("i", init.getName());
      assertNull(init.getTypeRef());
      assertEquals(new AstLiteral(NUMBER, "0"), init.getInit());
      assertNull(stmtFor.getCondition());
      assertEquals(List.of(), stmtFor.getBlock().getStatements());
    }
    {
      var stmtFor = (AstFor) statements.get(2);
      var init = stmtFor.getInit();
      assertEquals("i", init.getName());
      assertNull(init.getTypeRef());
      assertEquals(new AstLiteral(NUMBER, "0"), init.getInit());
      assertEquals(new AstNameRef("true"), stmtFor.getCondition());
      assertEquals(List.of(), stmtFor.getBlock().getStatements());
    }
  }

  @Test
  void testDeferStatement() {
    var module = parseFile("/parser/statements.ci");
    var function = module.getFunctions().get(2);
    assertEquals("d", function.getName());
    var statements = function.getBlock().getStatements();
    assertEquals(1, statements.size());
    {
      var stmtDefer = (AstDefer) statements.get(0);
      assertEquals(new AstNameRef("value"), stmtDefer.getExpression());
    }
  }
}
