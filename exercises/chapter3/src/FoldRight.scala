object FoldRight {

  // 使用foldRight计算List的长度
  def length[A](as: List[A]): Int = {
    as.foldRight(0)((_, b) => b + 1)
  }

  // 尾递归定义foldLeft
  @annotation.tailrec
  def foldLeft[A, B](as: List[A], z: B)(f: (B, A) => B): B = as match {
    case Nil => z
    case h :: t => foldLeft(t, f(z, h))(f)
  }

  // 用foldLeft计算的sum函数
  def sum(l: List[Int]) = l.foldLeft(0)(_+_)
  def product(l: List[Double]) = l.foldLeft(1.0)(_*_)
  def length2[A](l: List[A]): Int = l.foldLeft(0)((len, _) => len + 1)

  // 用fold实现颠倒顺序
  def reverse[A](as: List[A]): List[A] = as.foldLeft(Nil: List[A])((h, t) => t :: h)
  def reverse2[A](as: List[A]): List[A] = as.foldRight(Nil: List[A])((h, t) => t.appended(h))

  def foldRightViaFoldLeft[A,B](l: List[A], z: B)(f: (A,B) => B): B =
    reverse[A](l).foldLeft(z)((b, a) => f(a, b))

  // 这个是通过delay function实现的。
  // 也就是虽然是foldRight，但是每一次的函数都先不evaluate，等到foldRight完成之后再evaluate，这样顺序就相当于是foldLeft了
  // 这个偏理论，实际上也不是stack safe
  def foldLeftViaFoldRight[A,B](l: List[A], z: B)(f: (B,A) => B): B =
    l.foldRight(identity[B]_)((a, g) => b => g(f(b, a)))(z)

  def appendViaFoldRight[A](l: List[A], r: List[A]): List[A] = l.foldRight(r)(_ :: _)

  def concat[A](l: List[List[A]]): List[A] = l.foldRight(Nil: List[A])(appendViaFoldRight)

  def main(args: Array[String]): Unit = {
    val l = List("aaa", "bbb", "ccc", "ddd")
    println(length[String](l))
    println(foldLeft[String, Int](l, 0)((i, str) => i + str.size))
    println(sum(List(1,2,3,4,5)))
    println(product(List(1,2,3,4,5)))
    println(length2[String](l))
    println(reverse[String](l))
    println(reverse2[String](l))
    println(foldRightViaFoldLeft[String, Int](l, 0)((str, i) => i + str.size))
    println(foldLeftViaFoldRight[String, Int](l, 0)((i, str) => i + str.size))
    println(appendViaFoldRight[String](l, l))
    println(concat[String](List(l, l)))
  }
}
