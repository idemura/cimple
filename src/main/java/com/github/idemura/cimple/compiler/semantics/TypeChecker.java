package com.github.idemura.cimple.compiler.semantics;

import com.github.idemura.cimple.compiler.CompilerException;
import com.github.idemura.cimple.compiler.TypeRef;
import com.github.idemura.cimple.compiler.ast.*;
import java.util.ArrayList;
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
    for (var f : node.getFunctions()) {
      assignParameterTypes(f);
      overloads.computeIfAbsent(f.getName(), (k) -> new ArrayList<>()).add(f);
    }

    // Variables collected as children visit pass. There relative order is preserved as
    // we add them. Thus, we check initializer only references a variable declared before.

    visitChildren(node);
    return null;
  }

  @Override
  protected Object visit(AstVariable node) {
    visitChildren(node);

    // var def = node.getVariableDef();
    //
    // VariableDef conflict;
    // if (getParent() instanceof AstModule) {
    //   conflict = variables.putGlobal(def.getName(), def);
    // } else {
    //   conflict = variables.put(def.getName(), def);
    // }
    // if (conflict != null) {
    //   throw CompilerException.builder()
    //       .formatMessage("Variable %s: duplicate definition", def.getName())
    //       .setLocation(def.getLocation())
    //       .build();
    // }
    //
    // var init = node.getInit();
    // if (init == null && def.getTypeRef() == null) {
    //   throw CompilerException.builder()
    //       .formatMessage("Variable %s: no type spec or initializer expression", def.getName())
    //       .setLocation(def.getLocation())
    //       .build();
    // }
    //
    // if (def.getTypeRef() == null) {
    //   def.setTypeRef(init.getTypeRef());
    // }
    //
    // // Now typeRef of the variable is populated.
    // checkNotNull(def.getTypeRef());
    //
    // // Check type compatibility if not the same.
    // if (init != null && init.getTypeRef() != def.getTypeRef()) {
    //   init = promoteExpression(init, def.getTypeRef());
    //   if (init == null) {
    //     throw CompilerException.builder()
    //         .formatMessage("Variable %s: incompatible initializer and type spec", def.getName())
    //         .setLocation(def.getLocation())
    //         .build();
    //   }
    //   node.setInit(init);
    // }
    return null;
  }

  @Override
  protected Object visit(AstFunction node) {
    variables.pushScope();
    for (var p : node.getParameters()) {
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
  protected Object visit(AstNameRef node) {
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
  protected Object visit(AstFunctionApply node) {
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
    // if (node.getCondition() != null && node.getCondition().getTypeRef() != BOOL) {
    //   throw CompilerException.builder()
    //       .formatMessage("for-condition must be boolean, got %s",
    // node.getCondition().getTypeRef())
    //       .setLocation(node.getLocation())
    //       .build();
    // }
    return null;
  }

  // private AstExpression promoteExpression(AstExpression expr, TypeRef resultType) {
  //   // Quick check for the same type.
  //   var exprType = expr.getTypeRef();
  //   if (exprType.equals(resultType)) {
  //     return expr;
  //   }
  //
  //   if (exprType.isPrimitive() != resultType.isPrimitive()) {
  //     return null;
  //   }
  //
  //   // If both are primitive, use convert function.
  //   if (exprType.isPrimitive()) {
  //     if (resultType == BuiltinTypes.INT64 && exprType == BuiltinTypes.INT32) {
  //       return new AstFunctionApply(null, "$i32_to_i64", List.of(expr));
  //     }
  //     if (resultType == BuiltinTypes.FLOAT64 && exprType == BuiltinTypes.FLOAT32) {
  //       return new AstFunctionApply(null, "$f32_to_f64", List.of(expr));
  //     }
  //   }
  //
  //   return null;
  // }

  static void assignParameterTypes(AstFunction func) {
    // function f(x, y: i32) { ... }
    // Resolve types right to left, if type is missing use the last one.

    TypeRef prevTypeRef = null;
    int n = func.getParameters().size();
    for (int i = 1; i <= n; i++) {
      var p = func.getParameters().get(n - i);
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
