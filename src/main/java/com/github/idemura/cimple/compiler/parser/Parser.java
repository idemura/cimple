package com.github.idemura.cimple.compiler.parser;

import static com.github.idemura.cimple.compiler.parser.Keyword.*;
import static com.github.idemura.cimple.compiler.parser.TokenType.*;

import com.github.idemura.cimple.compiler.CompilerException;
import com.github.idemura.cimple.compiler.Location;
import com.github.idemura.cimple.compiler.QualifiedName;
import com.github.idemura.cimple.compiler.ast.AstArrayAccess;
import com.github.idemura.cimple.compiler.ast.AstBind;
import com.github.idemura.cimple.compiler.ast.AstBlock;
import com.github.idemura.cimple.compiler.ast.AstCall;
import com.github.idemura.cimple.compiler.ast.AstCast;
import com.github.idemura.cimple.compiler.ast.AstDefer;
import com.github.idemura.cimple.compiler.ast.AstEntityRef;
import com.github.idemura.cimple.compiler.ast.AstExpression;
import com.github.idemura.cimple.compiler.ast.AstExpressionStatement;
import com.github.idemura.cimple.compiler.ast.AstFieldAccess;
import com.github.idemura.cimple.compiler.ast.AstFor;
import com.github.idemura.cimple.compiler.ast.AstFunction;
import com.github.idemura.cimple.compiler.ast.AstFunctionHeader;
import com.github.idemura.cimple.compiler.ast.AstFunctionType;
import com.github.idemura.cimple.compiler.ast.AstGoto;
import com.github.idemura.cimple.compiler.ast.AstIf;
import com.github.idemura.cimple.compiler.ast.AstModule;
import com.github.idemura.cimple.compiler.ast.AstNumberLiteral;
import com.github.idemura.cimple.compiler.ast.AstRecordType;
import com.github.idemura.cimple.compiler.ast.AstReturn;
import com.github.idemura.cimple.compiler.ast.AstStatement;
import com.github.idemura.cimple.compiler.ast.AstStringLiteral;
import com.github.idemura.cimple.compiler.ast.AstType;
import com.github.idemura.cimple.compiler.ast.AstTypeRef;
import com.github.idemura.cimple.compiler.ast.AstUnionType;
import com.github.idemura.cimple.compiler.ast.AstVariable;
import com.github.idemura.cimple.compiler.ast.AstVariableStatement;
import com.github.idemura.cimple.compiler.ast.UnionVariant;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;

// Parses token stream, type checks and build the AST.
public class Parser {
  private final TokenStream tokens;

  // For testing
  public static AstModule parseCode(String code) {
    return new Parser(new Tokenizer(code, null).split()).parse();
  }

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
      switch (keyword(tokens.current())) {
        case FUNCTION:
          module.definitions().add(parseFunction());
          break;
        case TYPE:
          module.definitions().add(parseType());
          break;
        case VAR:
          module.definitions().add(parseVariable(true));
          break;
        case CONST:
          module.definitions().add(parseVariable(false));
          break;
        default:
          throw CompilerException.builder()
              .formatMessage("Invalid module definition")
              .setLocation(tokens.current().location())
              .build();
      }
    }
    return module;
  }

  private AstType parseType() {
    takeKeyword(TYPE);
    return switch (keyword(tokens.current())) {
      case RECORD -> parseTypeRecord();
      case FUNCTION -> parseTypeFunction();
      case UNION -> parseTypeUnion();
      default ->
          throw CompilerException.builder()
              .formatMessage("Expected type declaration kind after type")
              .setLocation(tokens.current().location())
              .build();
    };
  }

  private AstRecordType parseTypeRecord() {
    var type = new AstRecordType();
    takeKeyword(RECORD);
    type.setName(takeIdentifier());
    tokens.take(LCURLY);
    var fields = new ImmutableList.Builder<AstVariable>();
    while (!tokens.takeIf(RCURLY)) {
      fields.add(parseVariable(keyword(tokens.current()) != CONST));
    }
    type.setFields(fields.build());
    return type;
  }

  private AstUnionType parseTypeUnion() {
    var type = new AstUnionType();
    takeKeyword(UNION);
    type.setName(takeIdentifier());
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
    variant.setTag(name.value());
    if (tokens.takeIf(LPAREN)) {
      variant.setValueType(parseTypeRef());
      tokens.take(RPAREN);
    }
    return variant;
  }

  private AstFunctionType parseTypeFunction() {
    var type = new AstFunctionType();
    type.setHeader(parseFunctionHeader());
    tokens.take(SEMICOLON);
    return type;
  }

  private void parseModuleName(AstModule module) {
    takeKeyword(MODULE);
    var tokenName = tokens.take(IDENTIFIER);
    module.setName(tokenName.value());
    module.setLocation(tokenName.location());
    tokens.take(SEMICOLON);
  }

  private AstFunction parseFunction() {
    var function = new AstFunction();
    function.setLocation(tokens.current().location());
    function.setHeader(parseFunctionHeader());
    function.setBlock(parseBlock());
    return function;
  }

  private AstVariable parseVariable(boolean mutable) {
    takeKeyword(mutable ? VAR : CONST);
    var variable = new AstVariable();
    if (mutable) {
      variable.setBit(AstVariable.MUTABLE);
    }
    variable.setLocation(tokens.current().location());
    variable.setName(takeIdentifier());
    if (tokens.current().is(IDENTIFIER)) {
      variable.setType(parseTypeRef());
    }
    if (tokens.takeIf(ASSIGN)) {
      variable.setExpression(parseExpression());
    }
    tokens.take(SEMICOLON);
    return variable;
  }

  private AstVariableStatement parseVariableStatement(boolean mutable) {
    var stmt = new AstVariableStatement();
    stmt.setVariable(parseVariable(mutable));
    return stmt;
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
    var keyword = keywordOrNull(tokens.current());
    if (keyword == null) {
      return parseExpressionStatement();
    }
    return switch (keyword) {
      case VAR -> parseVariableStatement(true);
      case CONST -> parseVariableStatement(false);
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
    stmt.setLocation(takeKeyword(RETURN));
    stmt.setExpression(parseExpression());
    tokens.take(SEMICOLON);
    return stmt;
  }

  private AstStatement parseIf() {
    var stmt = new AstIf();
    stmt.setLocation(takeKeyword(IF));
    var conditions = new ImmutableList.Builder<AstExpression>();
    var thenBlocks = new ImmutableList.Builder<AstBlock>();
    conditions.add(parseExpression());
    thenBlocks.add(parseBlock());
    while (takeKeywordIf(ELSE)) {
      if (takeKeywordIf(IF)) {
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
    stmt.setLocation(takeKeyword(FOR));
    if (keywordOrNull(tokens.current()) == VAR) {
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
    stmt.setLocation(takeKeyword(GOTO));
    stmt.setLabel(tokens.take(IDENTIFIER).value());
    tokens.take(SEMICOLON);
    return stmt;
  }

  private AstStatement parseDefer() {
    var stmt = new AstDefer();
    stmt.setLocation(takeKeyword(DEFER));
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
      call.setFunction(operatorEntityRef(operator));
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
      call.setFunction(operatorEntityRef(operator));
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
      call.setFunction(operatorEntityRef(operator));
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
      takeKeyword(TYPE);
      expr.setTypeRef(parseTypeRef());
      tokens.take(RBRACKET);
      return expr;
    }
    switch (tokens.current().type()) {
      case IDENTIFIER -> {
        return new AstEntityRef(takeIdentifier());
      }
      case NUMBER -> {
        var token = tokens.take();
        var expr = new AstNumberLiteral(token.value());
        expr.setLocation(token.location());
        return expr;
      }
      case STRING -> {
        var token = tokens.take();
        var expr = new AstStringLiteral(token.value());
        expr.setLocation(token.location());
        return expr;
      }
      default ->
          throw CompilerException.builder()
              .formatMessage("Primary expression expected")
              .setLocation(tokens.current().location())
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
    takeKeyword(FUNCTION);
    var header = new AstFunctionHeader();
    if (tokens.next().is(PERIOD)) {
      var objectType = new AstTypeRef(takeIdentifier());
      header.setObjectType(objectType);
      tokens.take(PERIOD);
    }
    header.setName(takeIdentifier());
    header.setParameters(parseParameters());
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
        variable.setName(takeIdentifier());
        if (tokens.current().is(IDENTIFIER)) {
          variable.setType(parseTypeRef());
        }
        parameters.add(variable);
      } while (tokens.takeIf(COMMA));
    }
    tokens.take(RPAREN);
    return parameters;
  }

  private AstTypeRef parseTypeRef() {
    return new AstTypeRef(takeIdentifier());
  }

  private AstEntityRef operatorEntityRef(Token token) {
    var ref = new AstEntityRef(new QualifiedName(token.type().symbolName()));
    ref.setLocation(token.location());
    return ref;
  }

  private QualifiedName takeIdentifier() {
    return new QualifiedName(tokens.take(IDENTIFIER).value());
  }

  private Location takeKeyword(Keyword keyword) {
    var token = tokens.take();
    var kw = keyword(token);
    if (kw != keyword) {
      throw CompilerException.builder()
          .formatMessage("Expected keyword '%s', found '%s'", keyword.symbolName(), kw)
          .setLocation(token.location())
          .build();
    }
    return token.location();
  }

  private boolean takeKeywordIf(Keyword keyword) {
    var token = tokens.current();
    if (keyword == keywordOrNull(token)) {
      tokens.take();
      return true;
    }
    return false;
  }

  private static Keyword keywordOrNull(Token token) {
    if (!token.is(IDENTIFIER)) {
      return null;
    }
    return Keyword.find(token.value());
  }

  private static Keyword keyword(Token token) {
    if (!token.is(IDENTIFIER)) {
      throw CompilerException.builder()
          .formatMessage("Expected keyword, found '%s'", token.type())
          .setLocation(token.location())
          .build();
    }
    var kw = Keyword.find(token.value());
    if (kw == null) {
      throw CompilerException.builder()
          .formatMessage("Expected keyword, found '%s'", token.type())
          .setLocation(token.location())
          .build();
    }
    return kw;
  }
}
