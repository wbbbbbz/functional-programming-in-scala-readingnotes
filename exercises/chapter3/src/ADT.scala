object ADT {

  sealed trait Tree[+A]
  case class Leaf[A](value: A) extends Tree[A]
  case class Branch[A](left: Tree[A], right: Tree[A]) extends Tree[A]

  def size[A](t: Tree[A]): Int = t match {
    case Leaf(_) => 1
    case Branch(l, r) => 1 + size(l) + size(r)
  }

  def maximum(t: Tree[Int]): Int = t match {
    case Leaf(a) => a
    case Branch(l, r) => maximum(l) max maximum(r)
  }

  def depth[A](t: Tree[A]): Int = t match {
    case Leaf(_) => 1
    case Branch(l, r) => (depth(l) max depth(r)) + 1
  }

  def fold[A,B](t: Tree[A])(f: A => B)(g: (B,B) => B): B = t match {
    case Leaf(a) => f(a)
    case Branch(l, r) => g(fold[A, B](l)(f)(g), fold[A, B](r)(f)(g))
  }

  def map[A,B](t: Tree[A])(f: A => B): Tree[B] = t match {
    case Leaf(a) => Leaf(f(a))
    case Branch(l, r) => Branch(map(l)(f), map(r)(f))
  }

  def sizeViaFold[A](t: Tree[A]): Int = fold[A, Int](t)(_ => 1)(_ + _ + 1)
  def maximumViaFold(t: Tree[Int]): Int = fold[Int, Int](t)(identity)(_ max _)
  def depthViaFold[A](t: Tree[A]): Int = fold[A, Int](t)(_ => 1)((_ + 1 max _ + 1))
  def mapViaFold[A,B](t: Tree[A])(f: A => B): Tree[B] = fold[A, Tree[B]](t)(a => Leaf(f(a)))(Branch(_, _))

  def main(args: Array[String]): Unit = {
    val tree = Branch[Int](Branch[Int](Leaf[Int](1), Leaf[Int](2)), Branch[Int](Leaf[Int](3), Leaf[Int](4)))
    println(size(tree))
    println(sizeViaFold(tree))
    println(maximum(tree))
    println(maximumViaFold(tree))
    println(depth(tree))
    println(depthViaFold(tree))
    println(map(tree)(i => "a" * i))
    println(mapViaFold(tree)(i => "a" * i))

  }


}
