package com.github.idemura.cimple.compiler;

import static com.google.common.base.Preconditions.checkNotNull;

import com.github.idemura.cimple.common.CimpleException;
import java.util.ArrayList;
import java.util.List;

public class CompilerException extends CimpleException {
  private final Location location;

  public static class Builder {
    private String message;
    private Location location;
    private List<String> details = new ArrayList<>();

    public Builder formatMessage(String message, Object... args) {
      this.message = message.formatted(args);
      return this;
    }

    public Builder addDetail(String message, Object... args) {
      details.add(message.formatted(args));
      return this;
    }

    public Builder setLocation(Location location) {
      this.location = location;
      return this;
    }

    public CompilerException build() {
      var sb = new StringBuilder();
      if (location != null) {
        sb.append(location).append(": ");
      }
      sb.append(checkNotNull(message));
      for (var d : details) {
        sb.append(d).append("\n");
      }
      return new CompilerException(sb.toString(), location);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public CompilerException(String message, Location location) {
    super(message);
    this.location = location;
  }

  public Location getLocation() {
    return location;
  }
}
