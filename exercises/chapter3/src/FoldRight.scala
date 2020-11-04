object FoldRight {

  // 使用foldRight计算List的长度
  def length[A](as: List[A]): Int = {
    as.foldRight(0)((_, b) => b + 1)
  }

  // 尾递归定义foldLeft
  @annotation.tailrec
  def foldLeft[A, B](as: List[A], z: B)(f: (B, A) => B): B = as match {
    case Nil => z
    case Cons(h, t) => foldLeft(t, f(z, t))(f)
  }

  // 用foldLeft计算的sum函数
  def sum(l: List[Int]) = l.foldLeft(0)(_+_)
  def product(l: List[Double]) = l.foldLeft(1.0)(_*_)
  def length2[A](l: List[A]): Int = l.foldLeft(0)((len, _) => len + 1)

  // 用fold实现颠倒顺序
  def reverse[A](as: List[A]): List[A] = as.foldLeft(Nil)((h, t) => Cons(t, h))
  def reverse2[A](as: List[A]): List[A] = as.foldRight(Nil)((t, h) => Cons(t, h))

  def foldRightViaFoldLeft[A,B](l: List[A], z: B)(f: (A,B) => B): B =
    l.foldLeft(Nil)((h, t) => Cons(t, h)).foldLeft(z)((b, a) => f(a, b))

  // 这个是通过delay function实现的。
  // 也就是虽然是foldRight，但是每一次的函数都先不evaluate，等到foldRight完成之后再evaluate，这样顺序就相当于是foldLeft了
  // 这个偏理论，实际上也不是stack safe
  def foldLeftViaFoldRight[A,B](l: List[A], z: B)(f: (B,A) => B): B =
    foldRight(l, (b:B) => b)((a,g) => b => g(f(b,a)))(z)

  def appendViaFoldRight[A](l: List[A], r: List[A]): List[A] = l.foldRight(r)(Cons(_, _))

  def concat[A](l: List[List[A]]): List[A] = l.foldRight(Nil: List[A])(appendViaFoldRight)
}
