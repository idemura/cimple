package com.github.idemura.cimple.compiler.semantics;

import com.github.idemura.cimple.compiler.ErrorConsumer;
import com.github.idemura.cimple.compiler.ast.AstArrayType;
import com.github.idemura.cimple.compiler.ast.AstFunctionType;
import com.github.idemura.cimple.compiler.ast.AstModule;
import com.github.idemura.cimple.compiler.ast.AstPointerType;
import com.github.idemura.cimple.compiler.ast.AstRecordType;
import com.github.idemura.cimple.compiler.ast.AstType;
import com.github.idemura.cimple.compiler.ast.AstUnionType;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

class TypeValidator {
  static void checkRecursiveTypeDefinitions(AstModule module, ErrorConsumer errorConsumer) {
    for (var definition : module.definitions()) {
      if (definition instanceof AstType type && isCheckedType(type)) {
        var path = Collections.newSetFromMap(new IdentityHashMap<AstType, Boolean>());
        if (containsTypeByValue(type, type, path)) {
          errorConsumer.errorAt(type.location(), "Recursive type definition: '%s'", type.name());
        }
      }
    }
  }

  private static boolean containsTypeByValue(AstType root, AstType type, Set<AstType> path) {
    if (!path.add(type)) {
      return false;
    }
    return switch (type) {
      case AstRecordType recordType -> recordContainsType(root, recordType, path);
      case AstUnionType unionType -> unionContainsType(root, unionType, path);
      case AstFunctionType functionType -> functionContainsType(root, functionType, path);
      default -> false;
    };
  }

  private static boolean recordContainsType(
      AstType root, AstRecordType recordType, Set<AstType> path) {
    for (var field : recordType.fields()) {
      if (containsDirectType(root, field.type(), path)) {
        return true;
      }
    }
    return false;
  }

  private static boolean unionContainsType(
      AstType root, AstUnionType unionType, Set<AstType> path) {
    for (var variant : unionType.variants()) {
      if (containsDirectType(root, variant.valueType(), path)) {
        return true;
      }
    }
    return false;
  }

  private static boolean functionContainsType(
      AstType root, AstFunctionType functionType, Set<AstType> path) {
    var header = functionType.header();
    if (containsDirectType(root, header.objectType(), path)
        || containsDirectType(root, header.resultType(), path)) {
      return true;
    }
    for (var parameter : header.parameters()) {
      if (containsDirectType(root, parameter.type(), path)) {
        return true;
      }
    }
    return false;
  }

  private static boolean containsDirectType(AstType root, AstType type, Set<AstType> path) {
    if (type == null || type instanceof AstPointerType || type instanceof AstArrayType) {
      return false;
    }
    if (type == root) {
      return true;
    }
    return isCheckedType(type) && containsTypeByValue(root, type, path);
  }

  private static boolean isCheckedType(AstType type) {
    return type instanceof AstRecordType
        || type instanceof AstUnionType
        || type instanceof AstFunctionType;
  }
}
