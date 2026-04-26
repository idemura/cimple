package com.github.idemura.cimple.compiler;

import static com.github.idemura.cimple.common.Resources.readResource;
import static com.github.idemura.cimple.compiler.BuiltinTypeRefs.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class ParserTest {
  private AstNode parseFile(String fileName) {
    var code = readResource(getClass(), fileName);
    return new Parser(new Tokenizer(null, code).split()).parse();
  }

  @Test
  void testModule() {
    var module = (AstModule) parseFile("/parser/global_defs.ci");
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
  void testIfStatement() {
    var module = (AstModule) parseFile("/parser/statement_if.ci");
    var function = module.getFunctions().get(0);
    assertEquals(
        List.of(new VariableDef("a", BOOL), new VariableDef("b", BOOL)), function.getParameters());
    var statements = function.getBlock().getStatements();
    assertEquals(3, statements.size());
    {
      var ifStatement = (AstIf) statements.get(0);
      assertEquals(1, ifStatement.getConditions().size());
      assertEquals(1, ifStatement.getThenBlocks().size());
      assertEquals(new AstNameRef("a"), ifStatement.getConditions().get(0));
      assertNull(ifStatement.getElseBlock());
    }
    {
      var ifStatement = (AstIf) statements.get(1);
      assertEquals(1, ifStatement.getConditions().size());
      assertEquals(1, ifStatement.getThenBlocks().size());
      assertEquals(new AstNameRef("a"), ifStatement.getConditions().get(0));
      assertNotNull(ifStatement.getElseBlock());
    }
    {
      var ifStatement = (AstIf) statements.get(2);
      assertEquals(2, ifStatement.getConditions().size());
      assertEquals(2, ifStatement.getThenBlocks().size());
      assertEquals(new AstNameRef("a"), ifStatement.getConditions().get(0));
      assertEquals(new AstNameRef("b"), ifStatement.getConditions().get(1));
      assertNotNull(ifStatement.getElseBlock());
    }
  }
}
