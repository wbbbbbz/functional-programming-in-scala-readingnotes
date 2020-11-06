FUNCTIONAL PROGRAMMING IN SCALA
The following set of sections represent the exercises contained in the book "Functional Programming in Scala", written by Paul Chiusano and Rúnar Bjarnason and published by Manning. This content library is meant to be used in tandem with the book. We use the same numeration for the exercises for you to follow them.

For more information about "Functional Programming in Scala" please visit its official website.

STRICT AND NON-STRICT FUNCTIONS
NOTE: This section is only for educational purposes. In Scala 2.13, scala.collection.immutable.Stream is deprecated and scala.collection.immutable.LazyList is recommended for replacement. For more information, check the Scala Stream and LazyList documentation.

Exercise 5.1:

Now let's write a few helper functions to make inspecting streams easier, starting with a function to convert a Stream to a List (which will force its evaluation):
```scala
def toList[A](s: Stream[A]): List[A] =
  s match {
    case Cons(h, t) => h() :: toList(t())
    case _ => List()
  }

val s = Stream(1, 2, 3)
toList(s) shouldBe 
List(1, 2, 3)
```
Exercise 5.2:

Let's continue by writing the function take for returning the first n elements of a Stream. Note that in the following implementation, we're using Stream's smart constructors cons and empty, defined as follows:
```scala
def cons[A](hd: => A, tl: => Stream[A]): Stream[A] = {
  lazy val head = hd
  lazy val tail = tl
  Cons(() => head, () => tail)
}

def empty[A]: Stream[A] = Empty
def take[A](s: Stream[A], n: Int): Stream[A] =
  s match {
    case Cons(h, t) if n > 0 => cons[A](h(), t().take(n - 
1
))
    case Cons(h, _) if n == 0 => cons[A](h(), Stream.empty)
    case _ => Stream.empty
  }

take(Stream(1, 2, 3), 2).toList shouldBe List(1, 2)
```
drop is similar to take, but skips the first n elements of a Stream instead:
```scala
def drop[A](s: Stream[A], n: Int): Stream[A] =
  s match {
    case Cons(_, t) if n > 0 => t().drop(n - 1)
    case _ => s
  }

drop(Stream(1, 2, 3, 4, 5), 2).toList shouldBe List(3, 4, 5)
```
Exercise 5.3:

We can also implement takeWhile to return all starting elements of a Stream that match a given predicate:
```scala
def takeWhile[A](s: Stream[A], f: A => Boolean): Stream[A] =
  s match {
    case Cons(h, t) if f(h()) => cons(h(), t() takeWhile f)
    case _ => Stream.empty
  }

takeWhile(Stream(1, 2, 3, 4, 5), (x: Int) => x < 3).toList shouldBe 
List(1, 2)

takeWhile(Stream(1, 2, 3, 4, 5), (x: Int) => x < 0).toList shouldBe 
Nil
```
SEPARATING PROGRAM DESCRIPTION FROM EVALUATION
Laziness lets us separate the description of an expression from the evaluation of that expression. This gives us a powerful ability — we may choose to describe a “larger” expression than we need, and then evaluate only a portion of it. As an example, let’s look at the function exists that checks whether an element matching a Boolean function exists in this Stream:
```scala
def exists(p: A => Boolean): Boolean = this match {
  case Cons(h, t) => p(h()) || t().exists(p)
  case _ => false
}
```
Exercise 5.4:

Let's implement forAll, a function that checks that all elements in the Stream match a given predicate. Note that the implementation will stop as soon as it encounters a non-matching value.
```scala
def forAll[A](s: Stream[A], f: A => Boolean): Boolean =
  s.foldRight(
true
)((a, b) => f(a) && b)

forAll(Stream(1, 2, 3), (x: Int) => x % 2 == 0) shouldBe false
forAll(Stream("a", "b", "c"), (x: String) => x.size > 0) shouldBe true
```
Exercise 5.5:

Let's put foldRight to good use, by implementing takeWhile based on it:
```scala
def takeWhile_1(f: A => Boolean): Stream[A] =
  foldRight(empty[A])((h, t) =>
    if (f(h)) cons(h, t)
    else empty)
```
Exercise 5.6:

We can also do the same with headOption:
```scala
def headOption: Option[A] = foldRight(None: Option[A])((h, _) => Some(h))
```
Exercise 5.7:

Implementations for map, filter, append and flatMap using foldRight should sound familiar already:
```scala
def map[B](f: A => B): Stream[B] = foldRight(empty[B])((h, t) => cons(f(h), t))

def filter(f: A => Boolean): Stream[A] = foldRight(empty[A])((h, t) =>
  if (f(h)) cons(h, t)
  else t)

def append[B >: A](s: => Stream[B]): Stream[B] = foldRight(s)((h, t) => cons(h, t))

def flatMap[B](f: A => Stream[B]): Stream[B] = foldRight(empty[B])((h, t) => f(h) append t)
```
Exercise 5.x:

Let's look at a simplified program trace for the next piece of code.
```scala
Stream(1, 2, 3, 4).map(_ + 10).filter(_ % 2 == 0)
```
We'll convert that expression to a List to force evaluation. Try to follow with what's happening in each step:
```scala
val startingPoint = Stream(1, 2, 3, 4).map(_ + 10).filter(_ % 2 == 0).toList

// Apply map to the first element:
val step1 = cons(
11
, Stream(2, 3, 4).map(_ + 10)).filter(_ % 2 == 0).toList
// Apply filter to the first element:
val step2 = 
Stream(2, 3, 4)
.map(_ + 10).filter(_ % 2 == 0).toList
// Apply map to the second element:
val step3 = cons(12, 
Stream(3, 4)
.map(_ + 10)).filter(_ % 2 == 0).toList
// Apply filter to the second element. Produce the first element of the result:
val step4 = 12 :: Stream(3, 4).map(_ + 10).filter(_ % 2 == 0).toList
val step5 = 12 :: cons(
13
, 
Stream(4)
.map(_ + 10)).filter(_ % 2 == 0).toList
val step6 = 12 :: Stream(4).map(_ + 10).filter(_ % 2 == 0).toList
val step7 = 12 :: cons(
14
, Stream[Int]().map(_ + 10)).filter(_ % 2 == 0).toList
// Apply filter to the fourth element and produce the final element of the result.
val step8 = 12 :: 14 :: Stream[Int]().map(_ + 10).filter(_ % 2 == 0).toList
// map and filter have no more work to do, and the empty stream becomes the empty list.
val finalStep = 12 :: 14 :: List()
```
startingPoint shouldBe step1
step1 shouldBe step2
step2 shouldBe step3
step3 shouldBe step4
step4 shouldBe step5
step5 shouldBe step6
step6 shouldBe step7
step7 shouldBe step8
step8 shouldBe finalStep
INFINITE STREAMS AND CORECURSION
Exercise 5.x:

Here’s an example of an infinite Stream of 1s:
```scala
val ones: Stream[Int] = Stream.cons(1, ones)
```
The functions we’ve written so far only inspect the portion of the stream needed to generate the demanded output. Take a look at this example:
```scala
ones.take(5).toList shouldBe 
List(1,1,1,1,1)

ones.exists(_ % 2 != 0) shouldBe 
true

ones.map(_ + 1).exists(_ % 2 == 0) shouldBe 
true

ones.forAll(_ != 1) shouldBe 
false
```
Exercise 5.8:

Let's generalize ones slightly to the function constant, which returns an infinite Stream of a given value:
```scala
def constant[A](a: A): Stream[A] = {
  lazy val tail: Stream[A] = Cons(() => a, () => tail)
  tail
}
```
Exercise 5.9:

Of course, we can generate number series with Streams. For example, let's write a function that generates an infinite stream of integers (n, n + 1, n + 2...):
```scala
def from(n: Int): Stream[Int] =
  cons(n, from(n + 1))

from(100).take(5).toList shouldBe List(100, 101, 102, 103, 104)
```
Exercise 5.10:

We can also create a function fibs that generates the infinite stream of Fibonacci numbers: 0, 1, 1, 2, 3, 5, 8...
```scala
val fibs = {
  def go(f0: Int, f1: Int): Stream[Int] =
    cons(f0, go(f1, f0 + f1))
  go(0, 1)
}

fibs.take(7).toList shouldBe List(0, 1, 1, 2, 3, 5, 8)
```
Exercise 5.11:

Now we're going to write a more general stream-building function: unfold which takes an initial state, and a function for building both the next state and the next value in the stream to be generated:
```scala
def unfold[A, S](z: S)(f: S => Option[(A, S)]): Stream[A] = f(z) match {
  case Some((h, s)) => cons(h, unfold(s)(f))
  case None => empty
}
```
Exercise 5.12:

Now that we have unfold, let's rewrite our previous generator functions based on it, starting from fibs:
```scala
val fibsViaUnfold =
  unfold((0, 1)) { case (f0, f1) => Some((f0, (f1, f0 + f1))) }

fibsViaUnfold.take(7).toList shouldBe List(0, 1, 1, 2, 3, 5, 8)
```
from follows a similar principle, albeit a little simpler:
```scala
def fromViaUnfold(n: Int) = unfold(n)(n => Some((n, n + 1)))

fromViaUnfold(100).take(5).toList shouldBe List(100, 101, 102, 103, 104)
```
Again, constant can be implemented in terms of unfold in a very similar way:
```scala
def constantViaUnfold[A](a: A) = unfold(a)(_ => Some((a, a)))
```
Follow the same pattern to implement ones using unfold:
```scala
val onesViaUnfold = unfold(1)(_ => 
Some((1, 1)))

onesViaUnfold.take(10).toList shouldBe List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1)
```
Exercise 5.13:

Now we're going to re-implement some of the higher-order functions for Streams, starting with map:
```scala
def mapViaUnfold[B](f: A => B): Stream[B] = unfold(this) {
  case Cons(h, t) => Some((f(h()), t()))
  case _ => None
}
```
take can also be rewritten via unfold, let's try it:
```scala
def takeViaUnfold[A](s: Stream[A], n: Int): Stream[A] =
  unfold((s, n)) {
    case (Cons(h, t), 1) => Some((h(), (Stream.empty, 0)))
    case (Cons(h, t), n) if n > 1 => Some((h(), (t(), n - 1)))
    case _ => None
  }

takeViaUnfold(Stream(1, 2, 3, 4, 5), 5).toList shouldBe List(1, 2, 3, 4, 5)
```
Let's continue by rewritting takeWhile in terms of unfold:
```scala
def takeWhileViaUnfold(f: A => Boolean): Stream[A] = unfold(this) {
  case Cons(h, t) if f(h()) => Some((h(), t()))
  case _ => None
}
```
We can also bring back functions we saw previously with Lists, as zipWith:
```scala
def zipWith[B, C](s2: Stream[B])(f: (A, B) => C): Stream[C] = unfold((this, s2)) {
  case (Cons(h1, t1), Cons(h2, t2)) =>
    Some((f(h1(), h2()), (t1(), t2())))
  case _ => None
}
```
zipAll can also be implemented using unfold. Note that it should continue the traversal as long as either stream has more elements - it uses Option to indicate whether each stream has been exhausted:
```scala
def zipAll[B](s2: Stream[B]): Stream[(Option[A], Option[B])] = zipWithAll(s2)((_, _))

def zipWithAll[B, C](s2: Stream[B])(f: (Option[A], Option[B]) => C): Stream[C] =
  Stream.unfold((this, s2)) {
    case (Empty, Empty) => None
    case (Cons(h, t), Empty) => Some(f(Some(h()), Option.empty[B]) -> (t(), empty[B]))
    case (Empty, Cons(h, t)) => Some(f(Option.empty[A], Some(h())) -> (empty[A] -> t()))
    case (Cons(h1, t1), Cons(h2, t2)) => Some(f(Some(h1()), Some(h2())) -> (t1() -> t2()))
  }
```
Exercise 5.14:

Now we're going to try to implement startsWith using the functions we've seen so far:
```scala
def startsWith[A](s: Stream[A]): Boolean =
  zipAll(s).takeWhile(!_._2.isEmpty) forAll {
    case (h, h2) => h == h2
  }
```
Exercise 5.15:

We can also write tails using unfold. tails returns the Stream of suffixes of a given Stream, starting with the original Stream. For example, for Stream(1,2,3), it should return Stream(Stream(1,2,3), Stream(2,3), Stream(3), Stream()).
```scala
def tails[A](s: Stream[A]): Stream[Stream[A]] =
  unfold(s) {
    case Empty => None
    case s1 => Some((s1, s1 drop 1))
  } append Stream(Stream.empty)

tails(Stream(1, 2, 3)).toList
  .map(_.toList) shouldBe List(List(1, 2, 3), List(2, 3), List(3), List())
```
Exercise 5.16:

We can generalize tails to the function scanRight, which is like a foldRight that returns a stream of the intermediate results:
```scala
def scanRight[B](z: B)(f: (A, => B) => B): Stream[B] =
  foldRight((z, Stream(z)))((a, p0) => {
    lazy val p1 = p0
    val b2 = f(a, p1._1)
    (b2, cons(b2, p1._2))
  })._2
For example:

Stream(1, 2, 3).scanRight(0)(_ + _).toList shouldBe 
List(6, 5, 3, 0)
```
