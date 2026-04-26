package com.github.idemura.cimple.compiler;

import static com.github.idemura.cimple.common.Resources.readResource;
import static com.github.idemura.cimple.compiler.BuiltinTypeRefs.INT;
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
    var m = (AstModule) parseFile("/parser/global_defs.ci");
    assertEquals("base_test", m.getName());
    var functions = m.getFunctions();
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
    var variables = m.getVariables();
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
}
