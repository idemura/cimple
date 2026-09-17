# Cimple (Ci) Language Specification

## Contents

## Motivation

* Small amount of language concepts. Easy to start and master the language.
* Simple grammar. Explicit constructs.
* Strong statical type system.
* Type inference in the heart.
* Compiled.
* Compatible with C. Use C libraries and create libraries for C in Cimple.
* Grep-friendly. Even if you don't have access to the IDE, still can find the definition.
* Manual memory management, but safer. Not safe, but safer. We write system software after all.
* Generics
* Built for a 64-bit CPU world (who uses 32 bits?)

The reader should be familiar with C before reading this.

## Lexical Structure

### Comments

Comments start with `#` and continue until the end of the line:

```
# File operations module
module file_io;

type struct File {
  # Fields
}

# Creates a file in /tmp
function createTemp(name string) File* {
}
```

There are no multiline comments.
Comments like `/* */` in C are not grep-friendly.
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
Starting or ending with an underscore is not allowed.

```
short_path
temp
Point
RandomGenerator
openConnection
action
arg1
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

Module names matter only for type names.

Module-level variables are file-private. A module-level variable is visible only inside the file
where it is declared and cannot be exported.

## Functions

```
<function> := "function" <function_name> "(" <argument_list>? ")" <type_ref>? (<block> | ";")
<function_name> := <identifier>
<type_ref> := <identifier> ("[]" | "*")*
<argument_list> := <argument> ("," <argument>)*
<argument> := <identifier> <type_ref>?
<block> := "{" <statement>* "}"
```

A function that ends with `;` has no body and is resolved at linking time.
This section is incomplete.

## Function Generics

Function generics are declared with a `generic` prefix:

```
generic (<type_name>, ...) function <function_name>(<argument_list>?) <type_ref>? (<block> | ";")
```

Examples:

```
generic (T) function size(a T[]) int64;
generic (T) function first(a T[]) T;
generic (T, U) function convert(x T, fallback U) U;
```

Generic parameters are type wildcards. Type arguments are inferred from function parameter types
only; callers do not write explicit type arguments.

Every declared wildcard must appear in the function parameter types. Result type usage does not make
a wildcard inferable, and wildcard uses inside the function body do not count.

Not allowed:

```
generic (T) function make() T;
generic (T, U) function convert(x T) U;
```

`T[]` is supported because arrays are treated as type families. Pointer generics are not defined
yet, so `T*`, `T[]*`, and other wildcard-under-pointer forms are unsupported.

Generic and non-generic functions use the same global function lookup model: a function is resolved
by its name and parameter types. A wildcard signature that overlaps a concrete signature is not
allowed:

```
function f(a int[]);
generic (T) function f(a T[]);
```

This rule keeps lookup predictable and avoids a separate generic function namespace. It also keeps
the language close to C linkage while still allowing generic families where the parameter types make
the choice unambiguous.

# Function resolution

The goal of function resolution in Ci is to be simple, unambiguous, and easy to perform mentally.
Functions do not belong to modules. They share one global namespace and are resolved by their name
and parameter types. A function's defining module does not participate in lookup.
Parameter types retain their module identity, so functions can still be associated with types from
different modules without placing the functions themselves into those modules.

The following rules apply within a group of functions that have the same name and the same number
of parameters. A function's result type does not participate in resolution. These rules currently
cover non-generic functions only; generic function resolution is intentionally left unspecified.

First, consider resolution without interfaces. Before a call is resolved, all integer arguments are
promoted to `int`. The compiler then looks for an exact match between the argument types and the
function parameter types. There is no ranking of conversions and no search for a "best" overload.
That is the entire rule. Smaller integer types are storage types and cannot be used as function
parameter types.

Interfaces do not make resolution more complicated when arguments already have interface types.
Interfaces cannot inherit from one another, so an interface-typed argument can exactly match only
the same interface type. Every interface function has exactly one parameter without an explicit
type. That parameter has the enclosing interface type and is the argument used for virtual
dispatch:

```
type interface ISocket {
  function write(message string, socket);
}

type interface IFile {
  function write(message string, file);
}

function foo(file IFile, socket ISocket) {
  var message = "Hello";
  write(message, socket); # Exact match with the ISocket overload.
  write(message, file);   # Exact match with the IFile overload.
}
```

Ambiguity becomes possible because a concrete type may implement several interfaces:

```
type struct Sink {}

implement interface IFile(Sink) {
  function write(message string, sink) {
    # ...
  }
}

implement interface ISocket(Sink) {
  function write(message string, sink) {
    # ...
  }
}

function foo(sink Sink) {
  var message = "Hello";
  write(message, sink); # Invalid: Sink can be used as either IFile or ISocket.
}
```

Ci resolves every argument independently. At each argument position, the compiler collects the
compatible parameter types from the same name-and-arity group. A concrete argument is compatible
with its own type and with every interface it implements. Exactly one parameter type must be
compatible at each position. No compatible type means that no function matches; more than one means
that the argument is ambiguous. The compiler does not use other arguments to choose a type for an
ambiguous argument. This deliberately rejects calls that could otherwise be resolved by considering
the whole argument list. After every argument has resolved to one parameter type, the resulting
parameter list must exactly match a declared function:

```
type interface IFile {
  function write(message string, file);
}

type interface ISocket {
  function write(number int, socket);
}

type struct Sink {}

implement interface IFile(Sink) {
  function write(message string, sink) {
    # ...
  }
}

implement interface ISocket(Sink) {
  function write(number int, sink) {
    # ...
  }
}

function foo(sink Sink) {
  write("Hello", sink); # Invalid: the second argument can be IFile or ISocket.
  write(10, sink);      # Invalid for the same reason.
}
```

The first argument appears to select `IFile` in the first call and `ISocket` in the second call.
Nevertheless, both calls are invalid because the second argument is ambiguous by itself. This rule
avoids overload resolution whose result depends on reasoning across several arguments.

An explicit cast gives an expression one specific interface type and therefore makes resolution
unambiguous:

```
function foo(sink Sink) {
  var message = "Hello";
  write(message, (sink type ISocket)); # Selects the ISocket overload.
  write(message, (sink type IFile));   # Selects the IFile overload.
}
```

Interface values are pointer-like. A bare `null` can represent every interface or pointer type, but
Ci does not infer its type from an overload set. It therefore has no unique lookup type and must be
cast explicitly before it can be passed to a function:

```
function foo() {
  var message = "Hello";
  write(message, null);                  # Invalid: null has no unique lookup type.
  write(message, (null type IFile));     # Selects the IFile overload.
  write(message, (null type ISocket));   # Selects the ISocket overload.
}
```

An interface function declaration participates in the global function namespace. Functions defined
as part of an interface implementation are only virtual-dispatch targets. They do not become
additional functions in the global namespace and are not considered during ordinary name
resolution:

```
implement interface IFile(Sink) {
  function write(message string, sink) {
    # ...
  }
}
```

Defining the same function for `Sink` outside the implementation context is an error:

```
function write(message string, sink Sink) {
  # Invalid: overlaps the IFile and ISocket forms for Sink.
}
```

Such a function would introduce another possible meaning for an argument of type `Sink`. Ci rejects
the declaration instead of adding exact-match precedence or another overload-ranking rule. The
compiler reports a collision whenever a concrete overload and an interface overload in the same
name-and-arity group have a concrete/interface pair at the same position, regardless of their other
parameter types. The language favors rules that remain simple for humans over accepting every call
a compiler could theoretically disambiguate.

## Statements

```
<break_statement> := "break" ";"
```

`break;` exits the nearest enclosing loop.

## Types

There are several kinds of types in Cimple:

* Builtin
* Alias
* Struct
* Union
* Enum
* Function
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
string
```

`int` is a synonym for `int64`.

### Array

Ci arrays are resizable and may be reallocated. They store initialized elements and track both
their current size and their capacity. Elements are accessed with the indexing operator:

```
a[i]
```

Array operations for size, capacity, append, copy, and removal are intentionally left unspecified
while function overloading is being redesigned. They should be ordinary functions rather than a
separate dot-call syntax.

### Struct

```
type struct <name> {
}
```

### Union

```
type union <name> {
    None;
    Some(string);
}
```

### Enum

```
type enum <name> ["(" <baseType> ")"] {
    Red;
    Green(5);
    Blue;
}
```

If the base type is omitted, `int64` is used.

Enum variants are scoped to the enum namespace. They do not leak into the surrounding module,
function, or variable namespace.

Enum values follow the same assignment rules as C: the first variant is assigned `0` unless it has
an explicit value, and each variant without an explicit value is assigned the previous variant's
value plus `1`.

Every enum must define a variant with value `0`. This is required because Ci values may be
zero-initialized, so every enum type needs a valid zero value.

### Function Pointers

Function pointer types are defined with this syntax:

```
"type" "function" <type_name> "(" <argument_list>? ")" <type_ref>? ";"
```

Function values are invoked with postfix `!`:

```
<expression> "!" "(" <expression_list>? ")"
```

This syntax is intentionally different from named function calls. `f(x)` resolves `f` in the
function namespace, while `f!(x)` evaluates `f` as an expression and invokes the function value it
contains. This keeps function and variable namespaces separate even when they contain the same name:

```
type function Consumer(s string);

function print(s string) {
    # ...
}

function callPrinter(printer Consumer) {
    printer!("hello");
}
```

The expression before `!` must have a function type.

### Interface

```
type interface FileSystem {
    function open(fileSystem, name string) File*;
    function close(fileSystem, file File*);
}
```

Every function declared in an interface must have exactly one parameter without an explicit type.
Within the interface, this parameter has the enclosing interface type and identifies the argument
used for virtual dispatch. All other parameters must have explicit types.

Interfaces cannot inherit from other interfaces. Interface hierarchies are intentionally avoided so
that every interface definition is self-contained and grep-friendly.

Types implement interfaces explicitly. A type may implement several interfaces, but the interfaces
themselves remain independent of each other.

```
implement interface <interface_name>(<type_ref>) {
    <function_definition>*
}
```

All functions required by the interface must be defined inside the `implement interface` block.
Each implementation function must have exactly one untyped parameter at the same position as the
interface function's untyped parameter. In the implementation, this parameter has the concrete type
named by `<type_ref>`. Its name does not need to match the parameter name in the interface.

This keeps the implementation unit explicit: finding the block also finds the functions that
satisfy the interface. Implementation functions are virtual-dispatch targets and do not enter the
global function namespace as additional overloads.

## Operator Precedence

From highest to lowest:

| Operators                      | Associativity |
|--------------------------------|---------------|
| `(e)` `(e type t)`             | Left-to-right |
| `new` `.` `[]` `f(x)` `fp!(x)` | Left-to-right |
| `*` `/` `%`                    | Left-to-right |
| `+` `-`                        | Left-to-right |
| `<` `>` `>=` `<=`              | Left-to-right |
| `==` `!=`                      | Left-to-right |
| `&`                            | Left-to-right |
| `\|`                           | Left-to-right |
| `=` and op-shorthand           | None          |

Only one assignment per expression is allowed. Therefore, it does not matter whether it is
left-to-right or right-to-left. For simplicity, the parser parses it in left-to-right order.
Later, the semantic analyzer produces an error if more than one assignment appears in an
expression.

## Type Casting

When you need to cast a type, use this expression:

```
<cast-expr> := "(" <expression> "type" <type_ref> ")"
```

Its precedence is the same as that of a parenthesized expression.

## Code Style

This is recommendation and Cimple team default and promoted standard.
For modules: `name_with_underscores`.
For types: `PascalCase`. Acronyms treated as word: `HttpRequest`, `IoFile`.
Also, for enum/union tags.
For variables and functions: `camelCase`.
For compile-time constants: `ALL_CAPS`.

### Modules

Use snake_case for module names:

```
module ml_mathlib;
```
