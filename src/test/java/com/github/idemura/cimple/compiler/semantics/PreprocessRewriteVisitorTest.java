package com.github.idemura.cimple.compiler.semantics;

import static org.junit.jupiter.api.Assertions.*;

import com.github.idemura.cimple.compiler.ast.AstBoolLiteral;
import com.github.idemura.cimple.compiler.ast.AstDefer;
import com.github.idemura.cimple.compiler.ast.AstExpressionStatement;
import com.github.idemura.cimple.compiler.ast.AstFor;
import com.github.idemura.cimple.compiler.ast.AstIf;
import com.github.idemura.cimple.compiler.ast.AstNullLiteral;
import com.github.idemura.cimple.compiler.ast.AstReturn;
import com.github.idemura.cimple.compiler.ast.AstVariable;
import com.github.idemura.cimple.compiler.parser.Parser;
import com.github.idemura.cimple.compiler.tokens.Tokenizer;
import org.junit.jupiter.api.Test;

class PreprocessRewriteVisitorTest {
  @Test
  void testRewriteTrueFalseNullLiterals() {
    var code =
        """
        module test;

        function f() {
          if true {
          }
          defer null;
          var x = false;
          for var i = null; true; true {
          }
          return true;
        }
        """;

    var module = new Parser(new Tokenizer(code).split()).parse();
    module.accept(new PreprocessRewriteVisitor());

    var statements = module.functions().get(0).getBlock().statements();
    int i = 0;
    assertEquals(new AstBoolLiteral(true), ((AstIf) statements.get(i++)).getConditions().get(0));
    {
      var stmt = (AstDefer) statements.get(i++);
      var statements1 = stmt.getBlock().statements();
      assertEquals(1, statements1.size());
      assertEquals(
          new AstNullLiteral(), ((AstExpressionStatement) statements1.get(0)).getExpression());
    }
    assertEquals(new AstBoolLiteral(false), ((AstVariable) statements.get(i++)).getExpression());
    {
      var stmt = (AstFor) statements.get(i++);
      assertEquals(new AstNullLiteral(), stmt.getInit().getExpression());
      assertEquals(new AstBoolLiteral(true), stmt.getCondition());
      assertEquals(new AstBoolLiteral(true), stmt.getIncrement());
    }
    assertEquals(new AstBoolLiteral(true), ((AstReturn) statements.get(i++)).getExpression());
  }
}
