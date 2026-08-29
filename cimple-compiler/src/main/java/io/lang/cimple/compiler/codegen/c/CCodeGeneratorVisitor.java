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

class CCodeGeneratorVisitor extends AstVisitor {
  private final IndentWriter out;
  private final CCodeGeneratorParams params;

  CCodeGeneratorVisitor(IndentWriter out, CCodeGeneratorParams params) {
    this.out = out;
    this.params = params;
  }

  @Override
  protected void visit(AstModule node) {
    emitForwardDeclarations(node);
    for (var definition : node.definitions()) {
      if (definition instanceof AstEnumType) {
        definition.accept(this);
      }
    }
    for (var definition : node.definitions()) {
      if (definition instanceof AstType && !(definition instanceof AstEnumType)) {
        definition.accept(this);
      }
    }
    for (var definition : node.definitions()) {
      if (!(definition instanceof AstType)) {
        definition.accept(this);
      }
    }
  }

  @Override
  protected void visit(AstFunction node) {
    // TODO: Emit a C function definition.
  }

  @Override
  protected void visit(AstVariable node) {
    // TODO: Emit a C global variable definition.
  }

  @Override
  protected void visit(AstStructType type) {
    var name = cTypeName(type.name());
    out.writeLine("struct %s {".formatted(name));
    out.indent();
    for (var field : type.fields()) {
      out.writeLine("%s %s;".formatted(cType(field.type()), field.name().entity()));
    }
    out.unindent();
    out.writeLine("};");
    out.writeLine("");
  }

  @Override
  protected void visit(AstUnionType type) {
    var name = cTypeName(type.name());
    out.writeLine("struct %s {".formatted(name));
    out.indent();
    out.writeLine("int64_t tag;".formatted(name));
    if (type.hasPayload()) {
      out.writeLine("union {");
      out.indent();
      for (var variant : type.variants()) {
        if (variant.valueType() != null) {
          out.writeLine("%s %s;".formatted(cType(variant.valueType()), variant.tag()));
        }
      }
      out.unindent();
      out.writeLine("} u;");
    }
    out.unindent();
    out.writeLine("};");
    out.writeLine("");
  }

  @Override
  protected void visit(AstEnumType type) {
    out.writeLine("enum %s {".formatted(cTypeName(type.name())));
    out.indent();
    for (var variant : type.variants()) {
      out.writeLine(
          "%s_%s = %d,".formatted(cTypeName(type.name()), variant.tag(), variant.value()));
    }
    out.unindent();
    out.writeLine("};");
    out.writeLine("");
  }

  private void emitForwardDeclarations(AstModule module) {
    var emitted = false;
    for (var definition : module.definitions()) {
      if (definition instanceof AstStructType type) {
        out.writeLine("struct %s;".formatted(cTypeName(type.name())));
        emitted = true;
      } else if (definition instanceof AstUnionType type) {
        out.writeLine("struct %s;".formatted(cTypeName(type.name())));
        emitted = true;
      }
    }
    if (emitted) {
      out.writeLine("");
    }
  }

  private String cType(AstType type) {
    return switch (type) {
      case AstBuiltinType builtinType -> cBuiltinType(builtinType);
      case AstStringType ignored -> "const char*";
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
