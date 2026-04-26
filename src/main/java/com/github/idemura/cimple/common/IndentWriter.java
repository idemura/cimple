package com.github.idemura.cimple.common;

import java.io.IOException;
import java.io.UncheckedIOException;

public class IndentWriter {
  private final Appendable out;
  private final String indent;
  private int indentLevel;

  public IndentWriter(Appendable out, int indentWidth) {
    if (indentWidth < 0) {
      throw new IllegalArgumentException("indentWidth must be non-negative");
    }
    this.out = out;
    this.indent = " ".repeat(indentWidth);
  }

  public void indent() {
    indentLevel++;
  }

  public void unindent() {
    if (indentLevel == 0) {
      throw new IllegalStateException("Indent level is 0");
    }
    indentLevel--;
  }

  public void writeLine(String s) {
    try {
      for (var j = 0; j < indentLevel; j++) {
        out.append(indent);
      }
      out.append(s);
      out.append("\n");
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
