package com.github.idemura.cimple.compiler.semantics;

import static org.junit.jupiter.api.Assertions.*;

import com.github.idemura.cimple.compiler.ast.AstDefer;
import com.github.idemura.cimple.compiler.ast.AstExpressionStatement;
import com.github.idemura.cimple.compiler.ast.AstFor;
import com.github.idemura.cimple.compiler.ast.AstIf;
import com.github.idemura.cimple.compiler.ast.AstLiteral;
import com.github.idemura.cimple.compiler.ast.AstReturn;
import com.github.idemura.cimple.compiler.ast.AstTypeBuiltin;
import com.github.idemura.cimple.compiler.ast.AstVariable;
import com.github.idemura.cimple.compiler.ast.TypeRef;
import com.github.idemura.cimple.compiler.parser.Parser;
import com.github.idemura.cimple.compiler.tokens.Tokenizer;
import org.junit.jupiter.api.Test;

class PreprocessRewriteVisitorTest {
  @Test
  void testRewriteTrueFalseNullLiterals() {
    var code =
        """
        module test_semantics;

        function f() {
          if true {
          }
          defer null;
          var x = false;
          for var i = null; true; false {
          }
          return true;
        }
        """;

    var module = new Parser(new Tokenizer(code).split()).parse();
    module.accept(new PreprocessRewriteVisitor());

    var statements = module.functions().get(0).getBlock().statements();
    int i = 0;
    assertSame(AstLiteral.TRUE, ((AstIf) statements.get(i++)).getConditions().get(0));
    {
      var stmt = (AstDefer) statements.get(i++);
      var statements1 = stmt.getBlock().statements();
      assertEquals(1, statements1.size());
      assertSame(AstLiteral.NULL, ((AstExpressionStatement) statements1.get(0)).getExpression());
    }
    assertSame(AstLiteral.FALSE, ((AstVariable) statements.get(i++)).getExpression());
    {
      var stmt = (AstFor) statements.get(i++);
      assertSame(AstLiteral.NULL, stmt.getInit().getExpression());
      assertSame(AstLiteral.TRUE, stmt.getCondition());
      assertSame(AstLiteral.FALSE, stmt.getIncrement());
    }
    assertSame(AstLiteral.TRUE, ((AstReturn) statements.get(i++)).getExpression());
    assertEquals(TypeRef.of(AstTypeBuiltin.NULL), AstLiteral.NULL.getType());
  }
}
