package com.github.idemura.cimple.compiler;

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
    tokens.takeKeyword(TokenType.MODULE);
    var tokenName = tokens.take(TokenType.IDENTIFIER);
    module.setName(tokenName.value());
    module.setLocation(tokenName.location());
    tokens.take(TokenType.SEMICOLON);
  }

  private AstFunction parseFunction() {
    tokens.takeKeyword(TokenType.FUNCTION);
    var function = new AstFunction();
    parseFunctionQualifiedName(function);
    parseParameters(function);
    if (!tokens.current().is(TokenType.LCURLY)) {
      function.setResultType(parseTypeRef());
    }
    function.setBlock(parseBlock());
    return function;
  }

  private AstVariable parseVariable() {
    tokens.take(TokenType.VAR);
    var v = new AstVariable();
    var name = tokens.take(TokenType.IDENTIFIER);
    v.setLocation(name.location());
    v.setName(name.value());
    v.setTypeRef(parseTypeRef());
    if (tokens.takeIf(TokenType.ASSIGN) != null) {
      v.setInit(parseExpression());
    }
    tokens.take(TokenType.SEMICOLON);
    return v;
  }

  private AstBlock parseBlock() {
    var b = new AstBlock();
    // b.setLocation(tokens.current().location());
    tokens.take(TokenType.LCURLY);
    while (tokens.takeIf(TokenType.RCURLY) == null) {
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
    var keyword = tokens.takeKeyword(TokenType.RETURN);
    r.setLocation(keyword.location());
    r.setExpression(parseExpression());
    tokens.take(TokenType.SEMICOLON);
    return r;
  }

  private AstStatement parseIf() {
    var c = new AstIf();
    var keyword = tokens.take(TokenType.IF);
    // TODO: elif
    c.setLocation(keyword.location());
    c.setCondition(parseExpression());
    c.setThenBlock(parseBlock());
    if (tokens.takeIf(TokenType.ELSE) != null) {
      c.setElseBlock(parseBlock());
    }
    return c;
  }

  private AstStatement parseExpressionStatement() {
    var e = new AstExpressionStatement();
    e.setLocation(tokens.current().location());
    e.setExpression(parseExpression());
    tokens.take(TokenType.SEMICOLON);
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
    while (tokens.current().is(TokenType.CMP_LT) || tokens.current().is(TokenType.CMP_GT)) {
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
    while (tokens.current().is(TokenType.PLUS) || tokens.current().is(TokenType.MINUS)) {
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
    while (tokens.current().is(TokenType.ASTERISK) || tokens.current().is(TokenType.SLASH)) {
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
    if (tokens.takeIf(TokenType.LPAREN) != null) {
      var e = parseExpression();
      tokens.take(TokenType.RPAREN);
      return e;
    }
    var t = tokens.take();
    switch (t.type()) {
      case TokenType.IDENTIFIER -> {
        if (tokens.current().is(TokenType.LPAREN)) {
          var a = new AstFunctionApply();
          return a;
        } else {
          var n = new AstNameRef(t.value());
          n.setLocation(t.location());
          return n;
        }
      }
      case TokenType.NUMBER, TokenType.STRING -> {
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
    tokens.take(TokenType.LPAREN);
    if (tokens.takeIf(TokenType.RPAREN) == null) {
      do {
        result.add(parseExpression());
      } while (expressionListHasNext());
    }
    return result;
  }

  private boolean expressionListHasNext() {
    if (tokens.takeIf(TokenType.COMMA) != null) {
      return true;
    } else if (tokens.takeIf(TokenType.RPAREN) != null) {
      return false;
    } else {
      throw CompilerException.builder()
          .formatMessage("Invalid function call: : or ) expected")
          .setLocation(tokens.current().location())
          .build();
    }
  }

  private void parseFunctionQualifiedName(AstFunction function) {
    var first = tokens.take(TokenType.IDENTIFIER);
    function.setLocation(first.location());
    if (tokens.takeIf(TokenType.PERIOD) != null) {
      function.setBoundTypeName(first.value());
      function.setName(tokens.take(TokenType.IDENTIFIER).value());
    } else {
      function.setName(first.value());
    }
  }

  private void parseParameters(AstFunction function) {
    tokens.take(TokenType.LPAREN);
    while (tokens.takeIf(TokenType.RPAREN) == null) {
      var variable = new VariableDef();
      var tokenName = tokens.take(TokenType.IDENTIFIER);
      variable.setName(tokenName.value());
      variable.setLocation(tokenName.location());
      if (tokens.takeIf(TokenType.COMMA) != null) {
        // If we have only a name - this is a bound variable.
      } else {
        variable.setTypeRef(parseTypeRef());
      }
      function.addParameter(variable);
    }
  }

  private TypeRef parseTypeRef() {
    return new TypeRef(tokens.take(TokenType.IDENTIFIER).value());
  }
}
