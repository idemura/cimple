# Cimple Programming Language

Naming: start with letter. Starting with _ or having 2 or more _ in a row is reserved.

## Primitive Types

All integers are signed. Default - 64-bit integer. All primitives are 0-initialized by default.

```
# Comment
int8, int16, int32, int64 = int.
float32, float64
char
bool
byte - just bits, no arithmetics.
```

Literals:

```
5i8
5i16
5i32
5f32
5 (int or int64)
5.2f32
5f32
5.2f32
5.0 (float64)
$a (char)
$$ "$" char
$\\ "\" char)
$\t <tab> char, like in C
$\x5b hex code
true/false bool
```

## Variables (local)

```
var a int
var b = 10
var c int32 = 10
```

Global variables can use other global variables in init expression. Only defined above in the
code are visible (simple rule to ban cycles).

## Type casting

```
var x = 10.0
var y = (cast x + 5 : int32)
```

## Arrays

Array are always allocated on heap. Access only through index. Maybe bounds checked.

```
var *a = new int[80];
a[0] = 5;
delete a;
# or, defer:
defer delete a;
```

It can be captured by reference (unique ptr):

```
var &a = new int[80];
a[0] = 5;
# delete called automatically when scope ends
```

Arrays have built-in length() function. Index/size of type int.

```
var l = length(a);
```

## Strings

String is an immutable array of chars. `length` works for strings too.
Concatenation done with `+` (string must be on both sides of +).
Result must be deallocated. + never optimized even if both sides are literals:

```
var &s = "hello " + "world";
# Will be automatically deallocated when leave the scope.
```

Intermediate results of + don't need to be deallocated - compiler takes care of it:

```
var &name = "Igor";
var &s = "hello " + name + "!";
```

Above, allocation for name should be optimized by the compiler.

## Type names

Types can only appear in certain places lexically, thus types and variables/functions namespaces
do not overlap. Thus, variable/function and type with the same name allowed. Most of the times,
type is an identifier, but:

Array:

```
var a []int
```

Function

```
var f (int, int) int
```

Array of functions

```
var predicates [](int, int) bool
```

Array of pointers

```
var pointers []*string;
```

Pointer and reference are not part of the type. They are var/field modifier - now memory for a
class (type) is allocated. Reference is a unique pointer.

```
var *a = new Point2d(1, 2);
delete a;
var &b = new Point2d(1, 2); 
```

## Functions

```
function sum(x int, y int) int {
    return x + y;
} 
```

## Overload

Overloads are allowed explicitly, only on last arguments:

```
overload T int32
function write(format string, v T);

overload T bool
function write(format string, v T);
```

Overload allowed to have output of the same type as input:

```
overload T int32
function sum(x T, y T) T
```

In case of overload, all occurrences must be defined as overload.

## Templates

```
template T <typeclass>
struct Point2d {
    x T;
    y T;
};

template T
function sum(x T, y T) T {
    return x + y;
}

template T
function length(p Point2d<T>) T {
    return p.x * p.x + p.y * p.y;
}
```

## Namespaces

```
# Nested namespaces are not allowed.
namespace my_module;
```

Import names:

```
import my_module.func;
# Import all
import my_module._;
# Several
import my_module.(openFile, closeFile);
```

## Structs

```
struct Point2d {
    x int;
    y int;
}
```

Visibility:

* public - all fields visible outside current **file**.
* opaque - can create instances of this type, but fields are not accessible.

```
@public
struct Point2d {
    x int;
    y int;
}
```
