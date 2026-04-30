# Cimple (Ci) Language Specification

## Contents

## Motivation

* Statically typed
* Reduced amount of language concepts
* Compiled
* Compatible with C, but safer.
* Simple: easy to read and grep.
* Manual memory management
* Generics
* Built for a 64-bit CPU world (who uses 32 bits?)

The reader should be familiar with C before reading this.

## Lexical Structure

### Comments

Comments start with `#` and continue until the end of the line:

```
# File operations module
module file_io;

# Creates file in /tmp
function create_temp(string name) int {
}
```

There are no multiline comments.
Comments like `/* */` in C are not grepable.
Consider the following C code:

```
int file_copy(const char *source, const char *destination) {
    int fid_s = open(source);
    /*
    int fid_s = open(source, O_RDONLY);
    */
}
```

When you grep for `open`, you get two lines, and it is not immediately clear from the result that
the second occurrence is in a comment.

### Identifiers

An identifier starts with a letter and can contain letters, digits, or underscores.
Two or more underscores in a row are not allowed.

```
short_path
temp_
Point
RandomGenerator
openConnection
action
```

### Literals

Numbers start with a decimal digit. Integers do not contain a decimal point (`.`), while floats do.
Integer literals are of type `int64`, and float literals are of type `float64`.
When a literal is used to initialize a variable of a smaller type, or is passed as an argument of
a smaller type, the compiler checks that it is within the range of that smaller type.

String lietral is a UTF-8 text in double quotes (`"`). Escaping like in C.
Multiline string literal supported.

Boolean literals are the identifiers `true` and `false`.

`null` is a special null value.

## Modules

Each file must start with a module clause:

```
<module> := "module" <identifier>;
```

The Cimple compiler compiles all `.ci` files in the directory.
They **must** share the same module identifier.
Modules cannot be nested.

## Functions

```
<function> := "function" <function_name> "(" <argument_list>? ")" <type_ref>? <block>
<function_name> := <identifier> ("." <identfier>)?
<type_ref> := <identifier> ("[]" | "*")*
<argument_list> := <argument> ("," <argument>)*
<argument> := <identifier> <type_ref>?
<block> := "{" <statement>* "}"
```

There are two kinds of functions: free and bound.
This section is incomplete.

## Types

There are several kinds of types in Cimple:

* Builtin
* Record
* Union
* Function
* Alias
* Interface

User type definitions start with the keyword `type`.

### Builtin Types

Cimple has the following basic types:

```
void
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

`int` is a synonym for `int64` and `float` is a synonym for `float64`.

### Record

```
type record <name> {
}
```

### Union

```
type union <name> {
    None;
    Some(string);
}
```

### Function

```
type function Consumer(s string);
```

### Interface

```
type interface FileSystem {
    function open(name string) File*;
    function close(f File*);
}
```

### Alias

```
type alias Uri string;
```

## Operator Precedence

From highest to lowest:

| Operators            | Associativity |
|----------------------|---------------|
| `(e)` `[e type t]`   | Left-to-right |
| `.` `:` `[]` `f(x)`  | Left-to-right |
| `x++` `x--`          | Left-to-right |
| `++x` `--x` `~` `!`  | Right-to-left |
| `*` `/` `%`          | Left-to-right |
| `+` `-`              | Left-to-right |
| `<<` `>>` `>>>`      | Left-to-right |
| `&`                  | Left-to-right |
| `^`                  | Left-to-right |
| `\|`                 | Left-to-right |
| `<` `>` `>=` `<=`    | Left-to-right |
| `==` `!=`            | Left-to-right |
| `&&`                 | Left-to-right |
| `\|\|`               | Left-to-right |
| `=` and op-shorthand | None          |

Only one assignment per expression is allowed. Therefore, it does not matter whether it is
left-to-right or right-to-left. For simplicity, the parser parses it in left-to-right order.
Later, the semantic analyzer produces an error if more than one assignment appears in an
expression.

## Type Casting

When you need to cast a type, use this expression:

```
<cast-expr> := "[" <expression> "type" <type_ref> "]"
```

Its precedence is the same as that of a parenthesized expression.

## Code Style

### Modules

Use snake_case for module names:

```
module github_idemura_mathlib;
```
