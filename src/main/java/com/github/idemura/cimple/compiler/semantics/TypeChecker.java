package com.github.idemura.cimple.compiler.semantics;

import com.github.idemura.cimple.compiler.CompilerException;
import com.github.idemura.cimple.compiler.ast.AstBlock;
import com.github.idemura.cimple.compiler.ast.AstCall;
import com.github.idemura.cimple.compiler.ast.AstFor;
import com.github.idemura.cimple.compiler.ast.AstFunction;
import com.github.idemura.cimple.compiler.ast.AstIf;
import com.github.idemura.cimple.compiler.ast.AstLiteral;
import com.github.idemura.cimple.compiler.ast.AstModule;
import com.github.idemura.cimple.compiler.ast.AstName;
import com.github.idemura.cimple.compiler.ast.AstVariable;
import com.github.idemura.cimple.compiler.ast.AstVisitor;
import com.github.idemura.cimple.compiler.ast.TypeRef;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class TypeChecker extends AstVisitor {
  private final ScopeNameMap<AstVariable> variables = new ScopeNameMap<>();

  TypeChecker() {}

  @Override
  protected Object visit(AstModule node) {
    // Collect all functions. Then, we can collect variables - their init expressions may
    // reference functions.
    Map<String, List<AstFunction>> overloads = new HashMap<>();
    for (var f : node.functions()) {
      assignParameterTypes(f);
    }

    // Variables collected as children visit pass. There relative order is preserved as
    // we add them. Thus, we check initializer only references a variable declared before.

    visitChildren(node);
    return null;
  }

  @Override
  protected Object visit(AstVariable node) {
    visitChildren(node);
    return null;
  }

  @Override
  protected Object visit(AstFunction node) {
    variables.pushScope();
    for (var p : node.getHeader().getParameters()) {
      if (variables.put(p.getName(), p) != null) {
        throw CompilerException.builder()
            .formatMessage("Duplicate parameter %s", p.getName())
            .setLocation(p.getLocation())
            .build();
      }
    }
    visitChildren(node);
    variables.popScope();
    return null;
  }

  @Override
  protected Object visit(AstBlock node) {
    variables.pushScope();
    visitChildren(node);
    variables.popScope();
    return null;
  }

  @Override
  protected Object visit(AstLiteral node) {
    return null;
  }

  @Override
  protected Object visit(AstName node) {
    var v = variables.get(node.getName());
    if (v == null) {
      throw CompilerException.builder()
          .formatMessage("Undefined name %s", node.getName())
          .setLocation(node.getLocation())
          .build();
    }
    node.setVariable(v);
    return null;
  }

  @Override
  protected Object visit(AstCall node) {
    visitChildren(node);
    return null;
  }

  @Override
  protected Object visit(AstIf node) {
    visitChildren(node);
    for (var condition : node.getConditions()) {
      // if (condition.getTypeRef() != BOOL) {
      //   throw CompilerException.builder()
      //       .formatMessage("if-condition must be boolean, got %s", condition.getTypeRef())
      //       .setLocation(node.getLocation())
      //       .build();
      // }
    }
    return null;
  }

  @Override
  protected Object visit(AstFor node) {
    visitChildren(node);
    return null;
  }

  static void assignParameterTypes(AstFunction func) {
    TypeRef prevTypeRef = null;
    var parameters = func.getHeader().getParameters();
    int n = parameters.size();
    for (int i = 1; i <= n; i++) {
      var p = parameters.get(n - i);
      if (p.getTypeRef() == null) {
        if (prevTypeRef == null) {
          throw CompilerException.builder()
              .formatMessage("Function parameter type not specified")
              .setLocation(func.getLocation())
              .build();
        } else {
          // No deep copy.
          p.setTypeRef(prevTypeRef);
        }
      } else {
        prevTypeRef = p.getTypeRef();
      }
    }
  }
}
