FUNCTIONAL PROGRAMMING IN SCALA
The following set of sections represent the exercises contained in the book "Functional Programming in Scala", written by Paul Chiusano and Rúnar Bjarnason and published by Manning. This content library is meant to be used in tandem with the book. We use the same numeration for the exercises for you to follow them.

For more information about "Functional Programming in Scala" please visit its official website.

SINGLY LINKED LISTS
Assume the following functions are available for your reference:

```scala
sealed trait List[+A]
case object Nil extends List[Nothing]
case class Cons[+A](head: A, tail: List[A]) extends List[A]
```
Exercise 3.1:

Examine the next complex match expression. What will be the result?
```scala
val x = List(1, 2, 3, 4, 5) match {
  case Cons(x, Cons(2, Cons(4, _))) => x
  case Nil => 42
  case Cons(x, Cons(y, Cons(3, Cons(4, _)))) => x + y
  case Cons(h, t) => h + sum(t)
  case _ => 101
}
x shouldBe 3
```
Exercise 3.2:

Take a look at the implementation of List's tail function, and check its behaviour on the following cases:
```scala
def tail[A](l: List[A]): List[A] =
  l match {
    case Nil => sys.error("tail of empty list")
    case Cons(_, t) => t
  }
tail(List(1, 2, 3)) shouldBe List(2, 3)

tail(List(1)) shouldBe Nil
```
Exercise 3.3:

setHead follows a similar principle. Let's take a look at how it works:
```scala
def setHead[A](l: List[A], h: A): List[A] =
  l match {
    case Nil => sys.error("setHead on empty list")
    case Cons(_, t) => Cons(h, t)
  }
setHead(List(1, 2, 3), 3) shouldBe 
List(3, 2, 3)

setHead(List("a", "b"), "c") shouldBe 
List("c", "b")
```
Exercise 3.4:

We can generalize take to the function drop:
```scala
def drop[A](l: List[A], n: Int): List[A] =
  if (n <= 0) l
  else
    l match {
      case Nil => Nil
      case Cons(_, t) => drop(t, n - 1)
    }

drop(List(1, 2, 3), 1) shouldBe 
List(2, 3)

drop(List(1, 2, 3), 0) shouldBe 
List(1, 2, 3)

drop(List("a", "b"), 2) shouldBe 
Nil

drop(List(1, 2), 3) shouldBe 
Nil

drop(Nil, 1) shouldBe 
Nil
```
Exercise 3.5:

dropWhile extends the behaviour of drop, removing elements from the List prefix as long as they match a predicate. Study its implementation and check how it works with the following examples:
```scala
def dropWhile[A](l: List[A], f: A => Boolean): List[A] =
  l match {
    case Cons(h, t) if f(h) => dropWhile(t, f)
    case _ => l
  }

dropWhile(List(1, 2, 3), (x: Int) => x < 2) shouldBe 
List(2, 3)

dropWhile(List(1, 2, 3), (x: Int) => x > 2) shouldBe 
List(1, 2, 3)

dropWhile(List(1, 2, 3), (x: Int) => x > 0) shouldBe 
Nil

dropWhile(Nil, (x: Int) => x > 0) shouldBe 
Nil
```
Exercise 3.6:

init can be implemented in the same fashion, but cannot be implemented in constant time like tail:
```scala
def init[A](l: List[A]): List[A] =
  l match {
    case Nil => sys.error("init of empty list")
    case Cons(_, Nil) => Nil
    case Cons(h, t) => Cons(h, init(t))
  }
init(List(1, 2, 3)) shouldBe 
List(1, 2)

init(List(1)) shouldBe 
Nil
```
RECURSION OVER LISTS AND GENERALIZING TO HIGHER-ORDER FUNCTIONS
Exercise 3.x:

Let's run through the steps that foldRight will follow in our new implementation of sum2:
```scala
foldRight(Cons(1, Cons(2, Cons(3, Nil))), 0)((x, y) => x + y) shouldBe 6
1 + foldRight(Cons(2, Cons(3, Nil)), 0)((x, y) => x + y) shouldBe 6
1 + 2 + foldRight(Cons(3, Nil), 0)((x, y) => x + y) shouldBe 6
1 + 2 + 3 + foldRight(Nil, 0)((x, y) => x + y) shouldBe 6
1 + 2 + 3 + 0 shouldBe 6
 ```
Exercise 3.8:

Now that we know how foldRight works, try to think about what happens when you pass Nil and Cons themselves to foldRight.
```scala
foldRight(List(1, 2, 3), Nil: List[Int])(Cons(_, _)) shouldBe 
List(1, 2, 3)
```
Exercise 3.9:

Let's try to use foldRight to calculate the length of a list. Try to fill the gaps in our implementation:
```scala
def l = List(1, 2, 3, 4, 5)
def length[A](as: List[A]): Int = List.foldRight(as, 0)((_, acc) => acc + 1)

length(l) shouldBe 5
```
Exercise 3.10:

Let's write another general tail-recursive list-recursion function, foldLeft, using the techniques we discussed in the previous chapter:
```scala
def foldLeft[A, B](l: List[A], z: B)(f: (B, A) => B): B =
  l match {
    case Nil => z
    case Cons(h, t) => foldLeft(t, f(z, h))(f)
  }
  ```
Exercise 3.11:

Let's write functions sum, product and length of a list using foldLeft:
```scala
def sum3(l: List[Int]) = foldLeft(l, 0)(_ + _)
def product3(l: List[Double]) = foldLeft(l, 1.0)(_ * _)
def length2[A](l: List[A]): Int = foldLeft(l, 0)((acc, h) => acc + 1)

def listInts = List(1, 2, 3, 4, 5)
def listDoubles = List(1.0, 2.0, 3.0)
sum3(listInts) shouldBe 15
product3(listDoubles) shouldBe 6.0
length2(listInts) shouldBe 5
```
Exercise 3.12:

As we saw above, we can write the previous functions we implemented using foldRight with foldLeft. Let's continue with reverse:
```scala
def reverse[A](l: List[A]): List[A] = foldLeft(l, List[A]())((acc, h) => Cons(h, acc))
```
Exercise 3.13:

In fact, we can write foldLeft in terms of foldRight, and the other way around:
```scala
def foldRightViaFoldLeft[A, B](l: List[A], z: B)(f: (A, B) => B): B =
  foldLeft(reverse(l), z)((b, a) => f(a, b))

def foldLeftViaFoldRight[A, B](l: List[A], z: B)(f: (B, A) => B): B =
  foldRight(l, (b: B) => b)((a, g) => b => g(f(b, a)))(z)
```
Exercise 3.14:

Another function we can implement by using foldRight is append:
```scala
def appendViaFoldRight[A](l: List[A], r: List[A]): List[A] =
  foldRight(l, r)(Cons(_, _))
Take a look at its implementation and check how it works:

append(List(1, 2, 3), List(1, 2)) shouldBe 
List(1, 2, 3, 1, 2)

append(List(1, 2, 3), Nil) shouldBe 
List(1, 2, 3)

append(Nil, List(1, 2)) shouldBe 
List(1, 2)

append(Nil, Nil) shouldBe 
Nil
```
Exercise 3.15:

foldRight can also be useful to write a function concat that concatenates a list of lists into a single list. Take a look at its implementation:
```scala
def concat[A](l: List[List[A]]): List[A] =
  foldRight(l, Nil: List[A])(append)
```
Since append takes time proportional to its first argument, and this first argument never grows because of the right-associativity of foldRight, this function is linear in the total length of all lists.

MORE FUNCTIONS FOR WORKING WITH LISTS
Exercise 3.16:

Let's keep digging into the uses of foldLeft and foldRight, by implementing a function that transforms a list of integers by adding 1 to each element:
```scala
def add1(l: List[Int]): List[Int] = foldRight(l, Nil: List[Int])((h, t) => Cons(h + 
1, t))
add1(List(1, 2, 3)) shouldBe List(2, 3, 4)
```
Exercise 3.17:

We can do something similar to turn each value in a List[Double] into a String:
```scala
def doubleToString(l: List[Double]): List[String] =
  foldRight(l, Nil: List[String])((h, t) => Cons(h.toString, t))
```
Exercise 3.18:

Both add1 and doubleToString modify each element in a list while maintaining its structure. We can generalize it in the following way:
```scala
def map[A, B](l: List[A])(f: A => B): List[B] =
  foldRight(l, Nil: List[B])((h, t) => Cons(f(h), t))
```
Exercise 3.19:

Let's apply the same principle as we use in map to remove elements from a list, starting with a function to remove all odd numbers from a List[Int]:
```scala
def removeOdds(l: List[Int]): List[Int] =
  foldRight(l, Nil: List[Int])((h, t) => if (h % 2 == 0) Cons(h, t) else t)
removeOdds(List(1, 2, 3, 4, 5)) shouldBe List(2, 4)
```
Exercise 3.20:

We're going to implement a new function that works like map except that the function given will return a list instead of a single result, and that list should be inserted into the final resulting list:
```scala
def flatMap[A, B](l: List[A])(f: A => List[B]): List[B] =
  concat(map(l)(f))
```
Let's try it out:
```scala
flatMap(List(1, 2, 3))(i => List(i, i)) shouldBe 
List(1, 1, 2, 2, 3, 3)
```
Exercise 3.22:

Now we're going to write a function that accepts two lists of integers and constructs a new list by adding corresponding elements. i.e.: List(1, 2, 3) and List(4, 5, 6) become List(5, 7, 9):
```scala
def addPairwise(a: List[Int], b: List[Int]): List[Int] = (a, b) match {
  case (Nil, _) => Nil
  case (_, Nil) => Nil
  case (Cons(h1, t1), Cons(h2, t2)) => Cons(h1 + h2, addPairwise(t1, t2))
}
```
Exercise 3.23:

We can generalize the function above so that it's not specific to integers or addition, zipWith:
```scala
def zipWith[A, B, C](a: List[A], b: List[B])(f: (A, B) => C): List[C] = (a, b) match {
  case (Nil, _) => Nil
  case (_, Nil) => Nil
  case (Cons(h1, t1), Cons(h2, t2)) => Cons(f(h1, h2), zipWith(t1, t2)(f))
}
```
Let's try out zipWith in the following exercise:
```scala
zipWith(List("a", "b", "c"), List("A", "B", "C"))(_ + _) shouldBe 
List("aA", "bB", "cC")

zipWith(List(1, 2, 3), List(4, 5, 6))(_.toString + _.toString()) shouldBe 
List("14", "25", "36")
```
Exercise 3.24:

As a final example for working with lists, let's implement a hasSubsequence function for checking whether a List contains another List as a subsequence. For instance, List(1, 2, 3, 4) would have List(1, 2), List(2, 3) and List(4) as subsequences, among others:
```scala
@annotation.tailrec
def startsWith[A](l: List[A], prefix: List[A]): Boolean = (l, prefix) match {
  case (_, Nil) => true
  case (Cons(h, t), Cons(h2, t2)) if h == h2 => startsWith(t, t2)
  case _ => false
}

@annotation.tailrec
def hasSubsequence[A](sup: List[A], sub: List[A]): Boolean = sup match {
  case Nil => sub == Nil
  case _ if startsWith(sup, sub) => true
  case Cons(h, t) => hasSubsequence(t, sub)
}
```
Take a thorough look at the implementation of this function, and then try it out in the next exercise:
```scala
def l = List(1, 2, 3, 4, 5)

hasSubsequence(l, List(2, 3)) shouldBe 
true

hasSubsequence(l, List(0, 1)) shouldBe 
false

hasSubsequence(l, Nil) shouldBe 
true
```
TREES
Exercise 3.25:

Let's try to implement a function size to count the number of nodes (leaves and branches) in a tree:
```scala
def size[A](t: Tree[A]): Int =
  t match {
    case Leaf(_) => 1
    case Branch(l, r) => 1 + size(l) + size(r)
  }

def t = Branch(Branch(Leaf(1), Leaf(2)), Leaf(3))
size(t) shouldBe 5
```
Exercise 3.26:

Following a similar implementation, we can write a function maximum that returns the maximum element in a Tree[Int]:
```scala
def maximum(t: Tree[Int]): Int = t match {
  case Leaf(n) => n
  case Branch(l, r) => maximum(l) max maximum(r)
}
```
Exercise 3.27:

In the same fashion, let's implement a function depth that returns the maximum path length from the root of a tree to any leaf.
```scala
def depth[A](t: Tree[A]): Int =
  t match {
    case Leaf(_) => 0
    case Branch(l, r) => 1 + (depth(l) max depth(r))
  }
def t = Branch(Branch(Leaf(1), Leaf(2)), Leaf(3))
depth(t) shouldBe 2
```
Exercise 3.28:

We can also write a function map, analogous to the method of the same name on List, that modifies each element in a tree with a given function. Let's try it out in the following exercise:
```scala
def map[A, B](t: Tree[A])(f: A => B): Tree[B] =
  t match {
    case Leaf(a) => Leaf(f(a))
    case Branch(l, r) => Branch(map(l)(f), map(r)(f))
  }

def t = Branch(Branch(Leaf(1), Leaf(2)), Leaf(3))
Tree.map(t)(_ * 2) shouldBe 
Branch(Branch(Leaf(2), Leaf(4)), Leaf(6))
```
Exercise 3.29:

To wrap this section up, let's generalize size, maximum, depth and map, writing a new function fold that abstracts over their similarities:
```scala
def fold[A, B](t: Tree[A])(f: A => B)(g: (B, B) => B): B = t match {
  case Leaf(a) => f(a)
  case Branch(l, r) => g(fold(l)(f)(g), fold(r)(f)(g))
}
```
Let's try to reimplement size, maximum, depth, and map in terms of this more general function:
```scala
def sizeViaFold[A](t: Tree[A]): Int =
  fold(t)(a => 1)(1 + _ + _)

def maximumViaFold(t: Tree[Int]): Int =
  fold(t)(a => a)(_ max _)

def depthViaFold[A](t: Tree[A]): Int =
  fold(t)(a => 0)((d1, d2) => 1 + (d1 max d2))

def mapViaFold[A, B](t: Tree[A])(f: A => B): Tree[B] =
  fold(t)(a => Leaf(f(a)): Tree[B])(Branch(_, _))

def t = Branch(Branch(Leaf(1), Leaf(2)), Leaf(3))
sizeViaFold(t) shouldBe 5
maximumViaFold(t) shouldBe 3
depthViaFold(t) shouldBe 2
mapViaFold(t)(_ % 2 == 0) shouldBe 
Branch(Branch(Leaf(false), Leaf(true)), Leaf(false))
```