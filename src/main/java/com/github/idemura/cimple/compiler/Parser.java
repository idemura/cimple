package com.github.idemura.cimple.compiler;

import static com.github.idemura.cimple.compiler.TokenType.*;

import java.util.ArrayList;
import java.util.List;

// Parses token stream, type checks and build the AST.
public class Parser {
  private final TokenStream tokens;

  Parser(TokenStream tokens) {
    this.tokens = tokens;
  }

  AstNode parse() {
    return parseModule();
  }

  private AstModule parseModule() {
    var module = new AstModule();
    parseModuleName(module);

    // TODO: Parse imports

    while (!tokens.done()) {
      var token = tokens.current();
      switch (token.keyword()) {
        case FUNCTION:
          module.addFunction(parseFunction());
          break;
        case TYPE:
          throw new UnsupportedOperationException();
        case VAR:
          module.addVariable(parseVariable());
          break;
        case CONST:
          throw new UnsupportedOperationException();
        default:
          throw CompilerException.builder()
              .formatMessage("Invalid module definition")
              .setLocation(token.location())
              .build();
      }
    }
    return module;
  }

  private void parseModuleName(AstModule module) {
    tokens.takeKeyword(MODULE);
    var tokenName = tokens.take(IDENTIFIER);
    module.setName(tokenName.value());
    module.setLocation(tokenName.location());
    tokens.take(SEMICOLON);
  }

  private AstFunction parseFunction() {
    tokens.takeKeyword(FUNCTION);
    var function = new AstFunction();
    parseFunctionQualifiedName(function);
    parseParameters(function);
    if (!tokens.current().is(LCURLY)) {
      function.setResultType(parseTypeRef());
    }
    function.setBlock(parseBlock());
    return function;
  }

  private AstVariable parseVariable() {
    tokens.takeKeyword(VAR);
    var v = new AstVariable();
    var name = tokens.take(IDENTIFIER);
    v.setLocation(name.location());
    v.setName(name.value());
    if (tokens.current().is(IDENTIFIER)) {
      v.setTypeRef(parseTypeRef());
    }
    if (tokens.takeIf(ASSIGN)) {
      v.setInit(parseExpression());
    }
    tokens.take(SEMICOLON);
    return v;
  }

  private AstBlock parseBlock() {
    var b = new AstBlock();
    // b.setLocation(tokens.current().location());
    tokens.take(LCURLY);
    while (!tokens.takeIf(RCURLY)) {
      var current = tokens.current();
      switch (current.keyword()) {
        case VAR:
          b.add(parseVariable());
          break;
        case RETURN:
          b.add(parseReturn());
          break;
        case IF:
          b.add(parseIf());
          break;
        case MATCH:
          throw new UnsupportedOperationException();
        case WHILE:
          throw new UnsupportedOperationException();
        case DO:
          throw new UnsupportedOperationException();
        case GOTO:
          throw new UnsupportedOperationException();
        default:
          // TODO: Should be an expression with a side effect
          b.add(parseExpressionStatement());
          break;
      }
    }
    return b;
  }

  private AstStatement parseReturn() {
    var r = new AstReturn();
    var keyword = tokens.takeKeyword(RETURN);
    r.setLocation(keyword.location());
    r.setExpression(parseExpression());
    tokens.take(SEMICOLON);
    return r;
  }

  private AstStatement parseIf() {
    var c = new AstIf();
    var keyword = tokens.take(IF);
    // TODO: elif
    c.setLocation(keyword.location());
    c.setCondition(parseExpression());
    c.setThenBlock(parseBlock());
    if (tokens.takeIf(ELSE)) {
      c.setElseBlock(parseBlock());
    }
    return c;
  }

  private AstStatement parseExpressionStatement() {
    var e = new AstExpressionStatement();
    e.setLocation(tokens.current().location());
    e.setExpression(parseExpression());
    tokens.take(SEMICOLON);
    return e;
  }

  private AstExpression parseExpression() {
    return parseExpressionComparison();
  }

  private AstExpression parseExpressionComparison() {
    AstExpression expr = parseExpressionAdditive();
    if (expr == null) {
      return null;
    }
    while (tokens.current().is(CMP_LT) || tokens.current().is(CMP_GT)) {
      var operator = tokens.take();
      var m = parseExpressionAdditive();
      if (m == null) {
        throw CompilerException.builder()
            .formatMessage("Expected expression after %s", operator)
            .setLocation(operator.location())
            .build();
      }
      // expr = new AstFunctionApply(operator.location(), operator.toString(), List.of(expr, m));
      expr = new AstFunctionApply();
    }
    return expr;
  }

  private AstExpression parseExpressionAdditive() {
    AstExpression expr = parseExpressionMultiple();
    if (expr == null) {
      return null;
    }
    while (tokens.current().is(PLUS) || tokens.current().is(MINUS)) {
      var operator = tokens.take();
      var m = parseExpressionMultiple();
      if (m == null) {
        throw CompilerException.builder()
            .formatMessage("Expected expression after %s", operator)
            .setLocation(operator.location())
            .build();
      }
      // expr = new AstFunctionApply(operator.location(), operator.toString(), List.of(expr, m));
      expr = new AstFunctionApply();
    }
    return expr;
  }

  private AstExpression parseExpressionMultiple() {
    AstExpression expr = parseExpressionPrimary();
    if (expr == null) {
      return null;
    }
    while (tokens.current().is(ASTERISK) || tokens.current().is(SLASH)) {
      var operator = tokens.take();
      var m = parseExpressionPrimary();
      if (m == null) {
        throw CompilerException.builder()
            .formatMessage("Expected expression after %s", operator)
            .setLocation(operator.location())
            .build();
      }
      // expr = new AstFunctionApply(operator.location(), operator.toString(), List.of(expr, m));
      expr = new AstFunctionApply();
    }
    return expr;
  }

  private AstExpression parseExpressionPrimary() {
    if (tokens.takeIf(LPAREN)) {
      var e = parseExpression();
      tokens.take(RPAREN);
      return e;
    }
    var t = tokens.take();
    switch (t.type()) {
      case IDENTIFIER -> {
        if (tokens.current().is(LPAREN)) {
          var a = new AstFunctionApply();
          return a;
        } else {
          var n = new AstNameRef(t.value());
          n.setLocation(t.location());
          return n;
        }
      }
      case NUMBER, STRING -> {
        var l = new AstLiteral(t.type(), t.value());
        l.setLocation(t.location());
        return l;
      }
      default ->
          throw CompilerException.builder()
              .formatMessage("Primary expression expected")
              .setLocation(t.location())
              .build();
    }
  }

  private List<AstExpression> parseExpressionList() {
    List<AstExpression> result = new ArrayList<>();
    tokens.take(LPAREN);
    if (!tokens.takeIf(RPAREN)) {
      do {
        result.add(parseExpression());
      } while (expressionListHasNext());
    }
    return result;
  }

  private boolean expressionListHasNext() {
    if (tokens.takeIf(COMMA)) {
      return true;
    } else if (tokens.takeIf(RPAREN)) {
      return false;
    } else {
      throw CompilerException.builder()
          .formatMessage("Invalid function call: : or ) expected")
          .setLocation(tokens.current().location())
          .build();
    }
  }

  private void parseFunctionQualifiedName(AstFunction function) {
    function.setLocation(tokens.current().location());
    if (tokens.next() == PERIOD) {
      function.setBoundTypeName(tokens.take(IDENTIFIER).value());
      tokens.take(PERIOD);
    }
    function.setName(tokens.take(IDENTIFIER).value());
  }

  private void parseParameters(AstFunction function) {
    tokens.take(LPAREN);
    if (!tokens.current().is(RPAREN)) {
      do {
        var variable = new VariableDef();
        var tokenName = tokens.take(IDENTIFIER);
        variable.setName(tokenName.value());
        variable.setLocation(tokenName.location());
        if (tokens.current().is(IDENTIFIER)) {
          variable.setTypeRef(parseTypeRef());
        }
        function.addParameter(variable);
      } while (tokens.takeIf(COMMA));
    }
    tokens.take(RPAREN);
  }

  private TypeRef parseTypeRef() {
    return new TypeRef(tokens.take(IDENTIFIER).value());
  }
}
