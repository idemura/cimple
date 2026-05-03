package com.github.idemura.cimple.compiler.parser;

import static com.github.idemura.cimple.compiler.common.Keyword.*;
import static com.github.idemura.cimple.compiler.tokens.TokenType.*;

import com.github.idemura.cimple.compiler.CompilerException;
import com.github.idemura.cimple.compiler.ast.AstArrayAccess;
import com.github.idemura.cimple.compiler.ast.AstBind;
import com.github.idemura.cimple.compiler.ast.AstBlock;
import com.github.idemura.cimple.compiler.ast.AstCall;
import com.github.idemura.cimple.compiler.ast.AstCast;
import com.github.idemura.cimple.compiler.ast.AstDefer;
import com.github.idemura.cimple.compiler.ast.AstExpression;
import com.github.idemura.cimple.compiler.ast.AstExpressionStatement;
import com.github.idemura.cimple.compiler.ast.AstFieldAccess;
import com.github.idemura.cimple.compiler.ast.AstFor;
import com.github.idemura.cimple.compiler.ast.AstFunction;
import com.github.idemura.cimple.compiler.ast.AstFunctionHeader;
import com.github.idemura.cimple.compiler.ast.AstGoto;
import com.github.idemura.cimple.compiler.ast.AstIf;
import com.github.idemura.cimple.compiler.ast.AstModule;
import com.github.idemura.cimple.compiler.ast.AstName;
import com.github.idemura.cimple.compiler.ast.AstNumberLiteral;
import com.github.idemura.cimple.compiler.ast.AstReturn;
import com.github.idemura.cimple.compiler.ast.AstStatement;
import com.github.idemura.cimple.compiler.ast.AstStringLiteral;
import com.github.idemura.cimple.compiler.ast.AstType;
import com.github.idemura.cimple.compiler.ast.AstTypeAlias;
import com.github.idemura.cimple.compiler.ast.AstTypeFunction;
import com.github.idemura.cimple.compiler.ast.AstTypeRecord;
import com.github.idemura.cimple.compiler.ast.AstTypeUnion;
import com.github.idemura.cimple.compiler.ast.AstVariable;
import com.github.idemura.cimple.compiler.ast.QualifiedName;
import com.github.idemura.cimple.compiler.ast.TypeRef;
import com.github.idemura.cimple.compiler.ast.UnionVariant;
import com.github.idemura.cimple.compiler.tokens.Token;
import com.github.idemura.cimple.compiler.tokens.TokenStream;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;

// Parses token stream, type checks and build the AST.
public class Parser {
  private final TokenStream tokens;

  public Parser(TokenStream tokens) {
    this.tokens = tokens;
  }

  public AstModule parse() {
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
          module.functions().add(parseFunction());
          break;
        case TYPE:
          module.types().add(parseType());
          break;
        case VAR:
          module.variables().add(parseVariable(true));
          break;
        case CONST:
          module.variables().add(parseVariable(false));
          break;
        default:
          throw CompilerException.builder()
              .formatMessage("Invalid module definition")
              .setLocation(token.location())
              .build();
      }
    }
    return module;
  }

  private AstType parseType() {
    tokens.takeKeyword(TYPE);
    return switch (tokens.current().keyword()) {
      case FUNCTION -> parseTypeFunction();
      case RECORD -> parseTypeRecord();
      case UNION -> parseTypeUnion();
      case OPAQUE -> parseTypeAlias();
      default ->
          throw CompilerException.builder()
              .formatMessage("Expected type declaration kind after type")
              .setLocation(tokens.current().location())
              .build();
    };
  }

  private AstTypeRecord parseTypeRecord() {
    var type = new AstTypeRecord();
    tokens.takeKeyword(RECORD);
    var name = tokens.take(IDENTIFIER);
    type.setLocation(name.location());
    type.setName(new QualifiedName(name.value()));
    tokens.take(LCURLY);
    var fields = new ImmutableList.Builder<AstVariable>();
    while (!tokens.takeIf(RCURLY)) {
      fields.add(parseVariable(tokens.current().keyword() != CONST));
    }
    type.setFields(fields.build());
    return type;
  }

  private AstTypeUnion parseTypeUnion() {
    var type = new AstTypeUnion();
    tokens.takeKeyword(UNION);
    var name = tokens.take(IDENTIFIER);
    type.setLocation(name.location());
    type.setName(new QualifiedName(name.value()));
    tokens.take(LCURLY);
    var variants = new ImmutableList.Builder<UnionVariant>();
    while (!tokens.takeIf(RCURLY)) {
      variants.add(parseTypeUnionVariant());
      tokens.take(SEMICOLON);
    }
    type.setVariants(variants.build());
    return type;
  }

  private UnionVariant parseTypeUnionVariant() {
    var variant = new UnionVariant();
    var name = tokens.take(IDENTIFIER);
    variant.setLocation(name.location());
    variant.setName(name.value());
    if (tokens.takeIf(LPAREN)) {
      variant.setValueType(parseTypeRef());
      tokens.take(RPAREN);
    }
    return variant;
  }

  private AstTypeAlias parseTypeAlias() {
    var type = new AstTypeAlias();
    tokens.takeKeyword(OPAQUE);
    var name = tokens.take(IDENTIFIER);
    type.setLocation(name.location());
    type.setName(new QualifiedName(name.value()));
    type.setBaseTypeRef(parseTypeRef());
    tokens.take(SEMICOLON);
    return type;
  }

  private AstTypeFunction parseTypeFunction() {
    var type = new AstTypeFunction();
    var header = parseFunctionHeader();
    type.setHeader(header);
    type.setLocation(header.getLocation());
    tokens.take(SEMICOLON);
    return type;
  }

  private void parseModuleName(AstModule module) {
    tokens.takeKeyword(MODULE);
    var tokenName = tokens.take(IDENTIFIER);
    module.setName(tokenName.value());
    module.setLocation(tokenName.location());
    tokens.take(SEMICOLON);
  }

  private AstFunction parseFunction() {
    var function = new AstFunction();
    var header = parseFunctionHeader();
    function.setHeader(header);
    function.setLocation(header.getLocation());
    function.setBlock(parseBlock());
    return function;
  }

  private AstVariable parseVariable(boolean mutable) {
    tokens.takeKeyword(mutable ? VAR : CONST);
    var variable = new AstVariable();
    if (mutable) {
      variable.setBit(AstVariable.MUTABLE);
    }
    var name = tokens.take(IDENTIFIER);
    variable.setLocation(name.location());
    variable.setName(name.value());
    if (tokens.current().is(IDENTIFIER)) {
      variable.setTypeRef(parseTypeRef());
    }
    if (tokens.takeIf(ASSIGN)) {
      variable.setExpression(parseExpression());
    }
    tokens.take(SEMICOLON);
    return variable;
  }

  private AstBlock parseBlock() {
    var block = new AstBlock();
    tokens.take(LCURLY);
    while (!tokens.takeIf(RCURLY)) {
      block.statements().add(parseStatement());
    }
    return block;
  }

  private AstStatement parseStatement() {
    var keyword = tokens.current().keywordOrNull();
    if (keyword == null) {
      return parseExpressionStatement();
    }
    return switch (keyword) {
      case VAR -> parseVariable(true);
      case CONST -> parseVariable(false);
      case RETURN -> parseReturn();
      case IF -> parseIf();
      case FOR -> parseFor();
      case DEFER -> parseDefer();
      case MATCH -> throw new UnsupportedOperationException();
      case GOTO -> parseGoto();
      default ->
          throw CompilerException.builder()
              .formatMessage("Statement starts with unexpected keyword '%s'", keyword.symbolName())
              .setLocation(tokens.current().location())
              .build();
    };
  }

  private AstStatement parseReturn() {
    var stmt = new AstReturn();
    var location = tokens.takeKeyword(RETURN);
    stmt.setLocation(location);
    stmt.setExpression(parseExpression());
    tokens.take(SEMICOLON);
    return stmt;
  }

  private AstStatement parseIf() {
    var stmt = new AstIf();
    var location = tokens.takeKeyword(IF);
    stmt.setLocation(location);
    var conditions = new ImmutableList.Builder<AstExpression>();
    var thenBlocks = new ImmutableList.Builder<AstBlock>();
    conditions.add(parseExpression());
    thenBlocks.add(parseBlock());
    while (tokens.takeKeywordIf(ELSE)) {
      if (tokens.takeKeywordIf(IF)) {
        conditions.add(parseExpression());
        thenBlocks.add(parseBlock());
      } else {
        stmt.setElseBlock(parseBlock());
        break;
      }
    }
    stmt.setConditions(conditions.build());
    stmt.setThenBlocks(thenBlocks.build());
    return stmt;
  }

  private AstStatement parseFor() {
    var stmt = new AstFor();
    var location = tokens.takeKeyword(FOR);
    stmt.setLocation(location);
    if (tokens.current().keywordOrNull() == VAR) {
      stmt.setInit(parseVariable(true));
    }
    // Condition must be present, even if simple "true".
    stmt.setCondition(parseExpression());
    if (tokens.takeIf(SEMICOLON)) {
      stmt.setIncrement(parseExpression());
    }
    stmt.setBlock(parseBlock());
    return stmt;
  }

  private AstStatement parseGoto() {
    var stmt = new AstGoto();
    var location = tokens.takeKeyword(GOTO);
    stmt.setLocation(location);
    stmt.setLabel(tokens.take(IDENTIFIER).value());
    tokens.take(SEMICOLON);
    return stmt;
  }

  private AstStatement parseDefer() {
    var stmt = new AstDefer();
    var location = tokens.takeKeyword(DEFER);
    stmt.setLocation(location);
    if (tokens.current().is(LCURLY)) {
      stmt.setBlock(parseBlock());
    } else {
      var exprStmt = new AstExpressionStatement();
      exprStmt.setLocation(tokens.current().location());
      exprStmt.setExpression(parseExpression());
      tokens.take(SEMICOLON);
      var block = new AstBlock();
      block.statements().add(exprStmt);
      stmt.setBlock(block);
    }
    return stmt;
  }

  private AstStatement parseExpressionStatement() {
    var stmt = new AstExpressionStatement();
    stmt.setLocation(tokens.current().location());
    stmt.setExpression(parseExpression());
    tokens.take(SEMICOLON);
    return stmt;
  }

  private AstExpression parseExpression() {
    return parseComparisonChain();
  }

  private AstExpression parseComparisonChain() {
    var expr = parseAdditiveChain();
    if (expr == null) {
      return null;
    }
    while (tokens.current().is(CMP_LT) || tokens.current().is(CMP_GT)) {
      var operator = tokens.take();
      var m = parseAdditiveChain();
      if (m == null) {
        throw CompilerException.builder()
            .formatMessage("Expected expression after %s", operator)
            .setLocation(operator.location())
            .build();
      }
      var call = new AstCall();
      call.setFunction(operatorFunction(operator));
      call.setArgs(ImmutableList.of(expr, m));
      call.setLocation(operator.location());
      expr = call;
    }
    return expr;
  }

  private AstExpression parseAdditiveChain() {
    var expr = parseMultiplicativeChain();
    if (expr == null) {
      return null;
    }
    while (tokens.current().is(PLUS) || tokens.current().is(MINUS)) {
      var operator = tokens.take();
      var m = parseMultiplicativeChain();
      if (m == null) {
        throw CompilerException.builder()
            .formatMessage("Expected expression after %s", operator)
            .setLocation(operator.location())
            .build();
      }
      var call = new AstCall();
      call.setFunction(operatorFunction(operator));
      call.setArgs(ImmutableList.of(expr, m));
      call.setLocation(operator.location());
      expr = call;
    }
    return expr;
  }

  private AstExpression parseMultiplicativeChain() {
    var expr = parseFieldArrayCallChain();
    if (expr == null) {
      return null;
    }
    while (tokens.current().is(ASTERISK) || tokens.current().is(SLASH)) {
      var operator = tokens.take();
      var m = parseFieldArrayCallChain();
      if (m == null) {
        throw CompilerException.builder()
            .formatMessage("Expected expression after %s", operator)
            .setLocation(operator.location())
            .build();
      }
      var call = new AstCall();
      call.setFunction(operatorFunction(operator));
      call.setArgs(ImmutableList.of(expr, m));
      call.setLocation(operator.location());
      expr = call;
    }
    return expr;
  }

  private AstExpression parseFieldArrayCallChain() {
    var expr = parsePrimary();
    while (true) {
      var location = tokens.current().location();
      if (tokens.takeIf(PERIOD)) {
        var fieldAccess = new AstFieldAccess();
        fieldAccess.setObject(expr);
        fieldAccess.setFieldName(tokens.take(IDENTIFIER).value());
        expr = fieldAccess;
      } else if (tokens.takeIf(COLON)) {
        var bind = new AstBind();
        bind.setObject(expr);
        bind.setFunctionName(tokens.take(IDENTIFIER).value());
        expr = bind;
      } else if (tokens.takeIf(LBRACKET)) {
        var arrayAccess = new AstArrayAccess();
        arrayAccess.setArray(expr);
        arrayAccess.setIndex(parseExpression());
        tokens.take(RBRACKET);
        expr = arrayAccess;
      } else if (tokens.current().is(LPAREN)) {
        var call = new AstCall();
        call.setFunction(expr);
        call.setArgs(parseExpressionList());
        expr = call;
      } else {
        break;
      }
      expr.setLocation(location);
    }
    return expr;
  }

  // Parses:
  //  - (<expression>)
  //  - [<expression> type <type_ref>]
  //  - <name_ref> | <literal>
  private AstExpression parsePrimary() {
    if (tokens.takeIf(LPAREN)) {
      var expr = parseExpression();
      tokens.take(RPAREN);
      return expr;
    }
    if (tokens.takeIf(LBRACKET)) {
      var expr = new AstCast();
      expr.setExpression(parseExpression());
      expr.setLocation(tokens.takeKeyword(TYPE));
      expr.setTypeRef(parseTypeRef());
      tokens.take(RBRACKET);
      return expr;
    }
    var token = tokens.take();
    switch (token.type()) {
      case IDENTIFIER -> {
        var expr = new AstName(token.value());
        expr.setLocation(token.location());
        return expr;
      }
      case NUMBER -> {
        var expr = new AstNumberLiteral(token.value());
        expr.setLocation(token.location());
        return expr;
      }
      case STRING -> {
        var expr = new AstStringLiteral(token.value());
        expr.setLocation(token.location());
        return expr;
      }
      default ->
          throw CompilerException.builder()
              .formatMessage("Primary expression expected")
              .setLocation(token.location())
              .build();
    }
  }

  private List<AstExpression> parseExpressionList() {
    var result = new ImmutableList.Builder<AstExpression>();
    tokens.take(LPAREN);
    if (!tokens.takeIf(RPAREN)) {
      do {
        result.add(parseExpression());
      } while (expressionListHasNext());
    }
    return result.build();
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

  private AstFunctionHeader parseFunctionHeader() {
    tokens.takeKeyword(FUNCTION);
    var header = new AstFunctionHeader();
    header.setLocation(tokens.current().location());
    String boundTypeName = null;
    if (tokens.next() == PERIOD) {
      boundTypeName = tokens.take(IDENTIFIER).value();
      tokens.take(PERIOD);
    }
    header.setBoundTypeName(boundTypeName);
    header.setName(new QualifiedName(tokens.take(IDENTIFIER).value()));
    var parameters = parseParameters();
    header.setParameters(parameters);
    if (!tokens.current().is(LCURLY) && !tokens.current().is(SEMICOLON)) {
      header.setResultType(parseTypeRef());
    }
    return header;
  }

  private List<AstVariable> parseParameters() {
    var parameters = new ArrayList<AstVariable>();
    tokens.take(LPAREN);
    if (!tokens.current().is(RPAREN)) {
      do {
        var variable = new AstVariable();
        variable.setBit(AstVariable.PARAM);
        var tokenName = tokens.take(IDENTIFIER);
        variable.setName(tokenName.value());
        variable.setLocation(tokenName.location());
        if (tokens.current().is(IDENTIFIER)) {
          variable.setTypeRef(parseTypeRef());
        }
        parameters.add(variable);
      } while (tokens.takeIf(COMMA));
    }
    tokens.take(RPAREN);
    return parameters;
  }

  private TypeRef parseTypeRef() {
    return TypeRef.of(tokens.take(IDENTIFIER).value());
  }

  private AstName operatorFunction(Token operator) {
    var node = new AstName(operator.type().symbolName());
    node.setLocation(operator.location());
    return node;
  }
}
