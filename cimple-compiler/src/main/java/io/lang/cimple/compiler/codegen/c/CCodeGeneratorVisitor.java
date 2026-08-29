package io.lang.cimple.compiler.codegen.c;

import io.lang.cimple.compiler.Identifier;
import io.lang.cimple.compiler.IndentWriter;
import io.lang.cimple.compiler.ast.AstArrayType;
import io.lang.cimple.compiler.ast.AstBuiltinType;
import io.lang.cimple.compiler.ast.AstEnumType;
import io.lang.cimple.compiler.ast.AstFunction;
import io.lang.cimple.compiler.ast.AstFunctionType;
import io.lang.cimple.compiler.ast.AstModule;
import io.lang.cimple.compiler.ast.AstPointerType;
import io.lang.cimple.compiler.ast.AstStringType;
import io.lang.cimple.compiler.ast.AstStructType;
import io.lang.cimple.compiler.ast.AstType;
import io.lang.cimple.compiler.ast.AstUnionType;
import io.lang.cimple.compiler.ast.AstVariable;
import io.lang.cimple.compiler.ast.AstVisitor;
import java.util.ArrayList;
import java.util.List;

class CCodeGeneratorVisitor extends AstVisitor {
  private final IndentWriter out;
  private final CCodeGeneratorParams params;

  CCodeGeneratorVisitor(IndentWriter out, CCodeGeneratorParams params) {
    this.out = out;
    this.params = params;
  }

  @Override
  protected void visit(AstModule node) {
    var structs = collectStructs(node);
    var unions = collectUnions(node);
    var enums = collectEnums(node);
    emitStructForwardDeclarations(structs);
    emitEnumDefinitions(enums);
    emitStructDefinitions(structs);
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

  private static List<AstStructType> collectStructs(AstModule module) {
    var structs = new ArrayList<AstStructType>();
    for (var definition : module.definitions()) {
      if (definition instanceof AstStructType structType) {
        structs.add(structType);
      }
    }
    return structs;
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

  private static List<AstEnumType> collectEnums(AstModule module) {
    var enums = new ArrayList<AstEnumType>();
    for (var definition : module.definitions()) {
      if (definition instanceof AstEnumType enumType) {
        enums.add(enumType);
      }
    }
    return enums;
  }

  private void emitStructForwardDeclarations(List<AstStructType> structs) {
    for (var structType : structs) {
      out.writeLine("struct %s;".formatted(cTypeName(structType.name())));
    }
    if (!structs.isEmpty()) {
      out.writeLine("");
    }
  }

  private void emitStructDefinitions(List<AstStructType> structs) {
    for (var structType : structs) {
      var name = cTypeName(structType.name());
      out.writeLine("struct %s {".formatted(name));
      out.indent();
      for (var field : structType.fields()) {
        out.writeLine("%s %s;".formatted(cType(field.type()), field.name().entity()));
      }
      out.unindent();
      out.writeLine("};");
      out.writeLine("");
    }
  }

  private void emitUnionDefinitions(List<AstUnionType> unions) {
    for (var union : unions) {
      if (union.hasPayload()) {
        emitTaggedUnionDefinition(union);
      } else {
        emitEnumDefinition(cTypeName(union.name()), union);
      }
      out.writeLine("");
    }
  }

  private void emitEnumDefinitions(List<AstEnumType> enums) {
    for (var enumType : enums) {
      emitEnumDefinition(enumType);
      out.writeLine("");
    }
  }

  private void emitEnumDefinition(AstEnumType enumType) {
    out.writeLine("enum %s {".formatted(cTypeName(enumType.name())));
    out.indent();
    for (var variant : enumType.variants()) {
      out.writeLine(
          "%s_%s = %d,".formatted(cTypeName(enumType.name()), variant.tag(), variant.value()));
    }
    out.unindent();
    out.writeLine("};");
  }

  private void emitTaggedUnionDefinition(AstUnionType union) {
    var name = cTypeName(union.name());
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
      out.writeLine("%s_%s,".formatted(cTypeName(union.name()), variant.tag()));
    }
    out.unindent();
    out.writeLine("};");
  }

  private String cType(AstType type) {
    return switch (type) {
      case AstBuiltinType builtinType -> cBuiltinType(builtinType);
      case AstStringType ignored -> "char*";
      case AstStructType structType -> "struct " + cTypeName(structType.name());
      case AstEnumType enumType -> "enum " + cTypeName(enumType.name());
      case AstPointerType pointerType -> cType(pointerType.baseType()) + "*";
      case AstArrayType ignored ->
          throw new UnsupportedOperationException("C array type emission is not implemented yet");
      case AstFunctionType ignored ->
          throw new UnsupportedOperationException(
              "C function type emission is not implemented yet");
      case AstUnionType unionType ->
          "%s %s"
              .formatted(unionType.hasPayload() ? "struct" : "enum", cTypeName(unionType.name()));
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
    if (type == AstBuiltinType.INT8) {
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

  private String cTypeName(Identifier name) {
    if (params.mangleModuleName()) {
      return "%s__%s".formatted(name.module(), name.type());
    }
    return name.type();
  }
}
