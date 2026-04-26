# Cimple (Ci) Language Specification

## Contents

## Motivation

* Simple
* Compiled
* Statically typed
* Compatible with C
* Safer than C
* Simple generics

## Lexical Structure

### Identifiers

### Comments

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

## Code Style

### Modules

Use snake_case for module names:

```
module github_idemura_mathlib;
```
