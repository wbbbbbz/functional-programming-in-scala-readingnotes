object OtherListFunction {

  // 对每个元素加1
  def increment(l: List[Int]): List[Int] = l.foldRight(Nil: List[Int])((h, t) => h + 1 :: t)

  def doubleToString(l: List[Double]): List[String] = l.foldRight(Nil:List[String])((h,t) => h.toString :: t)

  // stack不安全。这种时候可以创造一个mutable的list，记录值再返回
  def map[A,B](l: List[A])(f: A => B): List[B] = l.foldRight(Nil:List[B])((h,t) => f(h) :: t)

  def map2[A,B](l: List[A])(f: A => B): List[B] = {
    import collection.mutable.ListBuffer
    val buf = new ListBuffer[B]
    @annotation.tailrec
    def go(cur: List[A]): List[B] = cur match {
      case Nil => List(buf.toList: _*)
      case h :: t => buf += f(h); go(t)
    }
    go(l)
  }

  // 也是一样，用foldRight不安全。用一个mutable的list
  def filter[A](l: List[A])(f: A => Boolean): List[A] = {
    import collection.mutable.ListBuffer
    val buf = new ListBuffer[A]
    @annotation.tailrec
    def go(cur: List[A]): List[A] = cur match {
      case Nil => List(buf.toList: _*)
      case h :: t => if (f(h)) buf += h;go(t)
    }
    go(l)
  }

  // 可以结合concat和map
  def flatMap[A, B](as: List[A])(f: A => List[B]): List[B] = {
    // as.foldRight(Nil: List[B])((h, t) => f(h).concat(t))
    // as.map(f).reduce(_.concat(_))
    as.foldLeft(List.empty[B])((h, t) => h.concat(f(t)))
  }

  // 直接通过map好像不能实装filter，因为不能return null？
  def filterViaFlatMap[A](l: List[A])(f: A => Boolean): List[A] = {
    l.flatMap((a : A) => if (f(a)) List(a) else Nil)
  }

  def addPairwise(a: List[Int], b: List[Int]): List[Int] = {
    // 非尾递归
    // case (Nil, _) => _
    // case (_, Nil) => _
    // case (ah :: at, bh :: bt) => ah + bh :: addPairwise(at, bt)
    import collection.mutable.ListBuffer
    val buf = new ListBuffer[Int]
    @annotation.tailrec
    def go(a: List[Int], b: List[Int]): List[Int] = (a, b) match {
      case (_, Nil) | (Nil, _) => buf.toList
      case (a, b) => buf += (a.head + b.head);go(a.tail, b.tail)
    }
    go(a, b)
  }

  def zipWith[A,B,C](a: List[A], b: List[B])(f: (A,B) => C): List[C] = {
    import collection.mutable.ListBuffer
    val buf = new ListBuffer[C]
    @annotation.tailrec
    def go(a: List[A], b: List[B])(f: (A,B) => C): List[C] = (a, b) match {
      case (_, Nil) | (Nil, _) => buf.toList
      case (a, b) => buf += f(a.head, b.head);go(a.tail, b.tail)(f)
    }
    go(a, b)(f)
  }

  def main(args: Array[String]): Unit = {
    val l = List(1, 2, 3, 4, 5)
    val l2 = List(1, 2, 3, 4, 5)
    val d = List(1.0, 2, 3, 4, 5)
    println(increment(l))
    println(doubleToString(d))
    println(map[Int, Double](l)(Math.pow(_, 2)))
    println(map2[Int, Double](l)(Math.pow(_, 2)))
    println(filter[Int](l)(_ % 2 == 0))
    println(flatMap[Int, Int](l)(i => List(i, i)))
    println(filterViaFlatMap[Int](l)(_ % 2 == 0))
    println(addPairwise(l, l2))
    println(zipWith(l, l2)(_ * _))
  }
}
