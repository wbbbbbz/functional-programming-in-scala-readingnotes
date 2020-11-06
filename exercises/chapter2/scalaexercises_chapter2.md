FUNCTIONAL PROGRAMMING IN SCALA
The following set of sections represent the exercises contained in the book "Functional Programming in Scala", written by Paul Chiusano and Rúnar Bjarnason and published by Manning. This content library is meant to be used in tandem with the book. We use the same numeration for the exercises for you to follow them.

For more information about "Functional Programming in Scala" please visit its official website.

TAIL-RECURSIVE FUNCTIONS
Exercise 2.1:

Try to fix the loop function inside fib so that it returns the correct values for each case in a tail-recursive way. What should the missing expressions for the trivial case and the recursive call be?

```scala
def fib(n: Int): Int = {
  @annotation.tailrec
  def loop(n: Int, prev: Int, cur: Int): Int =
    if (n <= 0) prev
    else loop(n - 1, cur, prev + cur)
  loop(n, 0, 1)
}

fib(5) should be(5)
```
HIGHER-ORDER FUNCTIONS
Exercise 2.2:

Let's do the same with isSorted. Take a detailed look at its implementation, what would be the results of applying the following anonymous functions to it?

```scala
def isSorted[A](as: Array[A], ordering: (A, A) => Boolean): Boolean = {
  @annotation.tailrec
  def go(n: Int): Boolean =
    if (n >= as.length - 1) true
    else if (!ordering(as(n), as(n + 1))) false
    else go(n + 1)
  go(0)
}

isSorted(Array(1, 3, 5, 7), (x: Int, y: Int) => x < y) shouldBe true

isSorted(Array(7, 5, 1, 3), (x: Int, y: Int) => x > y) shouldBe false

isSorted(
  Array("Scala", "Exercises"),
  (x: String, y: String) => x.length < y.length) shouldBe true
```
Exercise 2.3:

Currying is a transformation which converts a function f of two arguments into a function of one argument that partially applies f. Taking into account its signature, there's only one possible implementation that compiles. Take a look at its implementation and verify if this principle holds true in the following exercise:

```scala
def curry[A, B, C](f: (A, B) => C): A => (B => C) =
  a => b => f(a, b)

def f(a: Int, b: Int): Int = a + b
def g(a: Int)(b: Int): Int = a + b

curry(f)(1)(1) == f(1, 1) shouldBe true

curry(f)(1)(1) == g(1)(1) shouldBe true
```
Exercise 2.4:

Let's do the same with uncurrying is the reverse transformation of curry. Take a look at its implementation and check to see if this principle holds true:

```scala
def uncurry[A, B, C](f: A => B => C): (A, B) => C =
  (a, b) => f(a)(b)

def f(a: Int, b: Int): Int = a + b
def g(a: Int)(b: Int): Int = a + b

uncurry(g)(1, 1) == g(1)(1) shouldBe true

uncurry(g)(1, 1) == f(1, 1) shouldBe true
```
Exercise 2.5:

Function composing feeds the output of one function to the input of another function. Look at the implementation of compose and test its behavior on this exercise:
```scala
def compose[A, B, C](f: B => C, g: A => B): A => C =
  a => f(g(a))

def f(b: Int): Int = b / 2
def g(a: Int): Int = a + 2

compose(f, g)(0) == compose(g, f)(0) shouldBe false

compose(f, g)(2) shouldBe 2

compose(g, f)(2) shouldBe 3
```
