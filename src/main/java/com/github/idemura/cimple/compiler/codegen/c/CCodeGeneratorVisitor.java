package com.github.idemura.cimple.compiler.codegen.c;

import com.github.idemura.cimple.compiler.IndentWriter;
import com.github.idemura.cimple.compiler.ast.AstArrayType;
import com.github.idemura.cimple.compiler.ast.AstBuiltinType;
import com.github.idemura.cimple.compiler.ast.AstFunction;
import com.github.idemura.cimple.compiler.ast.AstFunctionType;
import com.github.idemura.cimple.compiler.ast.AstModule;
import com.github.idemura.cimple.compiler.ast.AstPointerType;
import com.github.idemura.cimple.compiler.ast.AstRecordType;
import com.github.idemura.cimple.compiler.ast.AstStringType;
import com.github.idemura.cimple.compiler.ast.AstType;
import com.github.idemura.cimple.compiler.ast.AstUnionType;
import com.github.idemura.cimple.compiler.ast.AstVariable;
import com.github.idemura.cimple.compiler.ast.AstVisitor;
import java.util.ArrayList;
import java.util.List;

class CCodeGeneratorVisitor extends AstVisitor {
  private final IndentWriter out;

  CCodeGeneratorVisitor(IndentWriter out) {
    this.out = out;
  }

  @Override
  protected void visit(AstModule node) {
    var records = collectRecords(node);
    var unions = collectUnions(node);
    emitRecordForwardDeclarations(records);
    emitRecordDefinitions(records);
    emitUnionDefinitions(unions);
    // TODO: Emit global variables.
    // TODO: Emit functions.
  }

  @Override
  protected void visit(AstFunction node) {
    // TODO: Emit a C function definition.
  }

  @Override
  protected void visit(AstVariable node) {
    // TODO: Emit a C global variable definition.
  }

  private static List<AstRecordType> collectRecords(AstModule module) {
    var records = new ArrayList<AstRecordType>();
    for (var definition : module.definitions()) {
      if (definition instanceof AstRecordType recordType) {
        records.add(recordType);
      }
    }
    return records;
  }

  private static List<AstUnionType> collectUnions(AstModule module) {
    var unions = new ArrayList<AstUnionType>();
    for (var definition : module.definitions()) {
      if (definition instanceof AstUnionType unionType) {
        unions.add(unionType);
      }
    }
    return unions;
  }

  private void emitRecordForwardDeclarations(List<AstRecordType> records) {
    for (var record : records) {
      out.writeLine("struct %s;".formatted(cTypeName(record)));
    }
    if (!records.isEmpty()) {
      out.writeLine("");
    }
  }

  private void emitRecordDefinitions(List<AstRecordType> records) {
    for (var record : records) {
      var name = cTypeName(record);
      out.writeLine("struct %s {".formatted(name));
      out.indent();
      for (var field : record.fields()) {
        out.writeLine("%s %s;".formatted(cType(field.type()), field.name().entityName()));
      }
      out.unindent();
      out.writeLine("};");
      out.writeLine("");
    }
  }

  private void emitUnionDefinitions(List<AstUnionType> unions) {
    for (var union : unions) {
      if (hasPayload(union)) {
        emitTaggedUnionDefinition(union);
      } else {
        emitEnumDefinition(cTypeName(union), union);
      }
      out.writeLine("");
    }
  }

  private void emitTaggedUnionDefinition(AstUnionType union) {
    var name = cTypeName(union);
    emitEnumDefinition(name + "_tag_", union);
    out.writeLine("struct %s {".formatted(name));
    out.indent();
    out.writeLine("enum %s_tag_ tag;".formatted(name));
    out.writeLine("union {");
    out.indent();
    for (var variant : union.variants()) {
      if (variant.valueType() != null) {
        out.writeLine("%s %s;".formatted(cType(variant.valueType()), variant.tag()));
      }
    }
    out.unindent();
    out.writeLine("} u;");
    out.unindent();
    out.writeLine("};");
  }

  private void emitEnumDefinition(String enumName, AstUnionType union) {
    out.writeLine("enum %s {".formatted(enumName));
    out.indent();
    for (var variant : union.variants()) {
      out.writeLine("%s_%s,".formatted(cTypeName(union), variant.tag()));
    }
    out.unindent();
    out.writeLine("};");
  }

  private static boolean hasPayload(AstUnionType union) {
    for (var variant : union.variants()) {
      if (variant.valueType() != null) {
        return true;
      }
    }
    return false;
  }

  private static String cType(AstType type) {
    return switch (type) {
      case AstBuiltinType builtinType -> cBuiltinType(builtinType);
      case AstStringType ignored -> "char*";
      case AstRecordType recordType -> "struct " + cTypeName(recordType);
      case AstPointerType pointerType -> cType(pointerType.baseType()) + "*";
      case AstArrayType ignored ->
          throw new UnsupportedOperationException("C array type emission is not implemented yet");
      case AstFunctionType ignored ->
          throw new UnsupportedOperationException(
              "C function type emission is not implemented yet");
      case AstUnionType unionType ->
          hasPayload(unionType)
              ? "struct " + cTypeName(unionType) + "_TaggedUnion"
              : "enum " + cTypeName(unionType);
      default -> throw new UnsupportedOperationException("Unsupported C type: " + type);
    };
  }

  private static String cBuiltinType(AstBuiltinType type) {
    if (type == AstBuiltinType.VOID) {
      return "void";
    }
    if (type == AstBuiltinType.BOOL) {
      return "bool";
    }
    if (type == AstBuiltinType.BYTE || type == AstBuiltinType.INT8) {
      return "int8_t";
    }
    if (type == AstBuiltinType.INT16) {
      return "int16_t";
    }
    if (type == AstBuiltinType.INT32) {
      return "int32_t";
    }
    if (type == AstBuiltinType.INT64) {
      return "int64_t";
    }
    if (type == AstBuiltinType.FLOAT32) {
      return "float32_t";
    }
    if (type == AstBuiltinType.FLOAT64) {
      return "float64_t";
    }
    if (type == AstBuiltinType.CHAR) {
      return "char";
    }
    throw new UnsupportedOperationException("Unsupported builtin C type: " + type);
  }

  private static String cTypeName(AstRecordType recordType) {
    return "%s__%s".formatted(recordType.name().moduleName(), recordType.name().typeName());
  }

  private static String cTypeName(AstUnionType unionType) {
    return "%s__%s".formatted(unionType.name().moduleName(), unionType.name().typeName());
  }
}
