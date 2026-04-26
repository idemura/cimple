package com.github.idemura.cimple.compiler;

import static com.github.idemura.cimple.common.Resources.readResource;
import static com.github.idemura.cimple.compiler.BuiltinTypeRefs.INT;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ParserTest {
  private AstNode parseFile(String fileName) {
    var code = readResource(getClass(), fileName);
    return new Parser(new Tokenizer(null, code).split()).parse();
  }

  @Test
  void testModule() {
    var m = (AstModule) parseFile("/parser/module.ci");
    assertEquals("base_test", m.getName());
    var functions = m.getFunctions();
    assertEquals(2, functions.size());
    {
      var f = functions.get(0);
      assertEquals("f", f.getName());
      assertEquals(INT, f.getResultType());
      var params = f.getParameters();
      assertEquals(1, params.size());
      var p0 = params.get(0);
      assertEquals("x", p0.getName());
      assertEquals(INT, p0.getTypeRef());
    }
    {
      var f = functions.get(1);
      assertEquals("g", f.getName());
      assertNull(f.getResultType());
      var params = f.getParameters();
      assertEquals(0, params.size());
    }
  }
}
