# Cimple (Ci) Language Specification

## Contents

## Motivation

* Statically typed
* Compiled
* Compatible with C, but safer.
* Simple: to read, to grep.
* Manual memory management
* Generics
* Built for 64-bit CPU world (who uses 32 bits?)

The reader should be familiar with C before reading this.

## Lexical Structure

### Comments

Comments start with `#` and continue till the end of the line:

```
# File operations module
module file_io;

# Creates file in /tmp
function create_temp(string name) int {
}
```

No multiline comments.
Comments like `/*` in C are not grep-able.
Consider the following C code:

```
int file_copy(const char *source, const char *destination) {
    int fid_s = open(source);
    /*
    int fid_s = open(source, O_RDONLY);
    */
}
```

When one greps for `open` they get two lines: and it is not clear right from the result that
the second occurrence is in comment.

### Identifiers

Identifier starts from a letter and can contain letters, digits or underscores.
Two or more underscores in a row not allowed.

```
short_path
temp_
Point
RandomGenerator
openConnection
action
```

### Fundamental Types

Cimple has following fundamental types:

```
bool
char
int8
int16
int32
int64
int
float32
float64
float
string
```

`int` is just a synonym for `int64` and float is a synonym for `float64`.

### Numbers

Numbers start with a decimal digit. Integers do not contain decimal point (`.`) while floats do.
Integer literals are of type `int64` and floats of type `float64`.
When used to init variable  of a smaller type or passed as a argument of a smaller type,
the compiler checks it is withing the smaller type range.

### Booleans

Booleans are two identifiers: `true` and `false`.

### Null

`null` is a special null pointer.

## Modules

Each file must start with a module clause:

```
<module> := "module" <identifier>;
```

The Cimple compiler compiles all `.ci` files in the directory.
They **must** share the same module identifier.
Modules can't be nested.

## Functions

```
<function> := "function" <function_name> "(" <argument_list>? ")" <type_ref>? <block>
<function_name> := <identifier> ("." <identfier>)?
<type_ref> := <identifier> ("[]" | "*")*
<argument_list> := <argument> ("," <argument>)*
<argument> := <identifier> <type_ref>?
<block> := "{" <statement>* "}"
```

There are two types of functions: free and bound.
Free is a normal

## Code Style

### Modules

Use snake_case for module names:

```
module github_idemura_mathlib;
```
