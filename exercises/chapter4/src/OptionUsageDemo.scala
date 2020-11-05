object OptionUsageDemo {

  def mean(xs: Seq[Double]): Option[Double] = xs match {
    case Nil => None
    case _ => Some(xs.sum / xs.length)
  }
  // 使用flatMap函数
  def variance(xs: Seq[Double]): Option[Double] = {
    mean(xs) flatMap (m => mean(xs.map(x => math.pow(x - m, 2))))
  }

  def map2[A, B, C](a: Option[A], b: Option[B])(f : (A, B) => C) : Option[C] = (a, b) match {
    case (None, _) | (_, None) => None
    case (Some(a), Some(b)) => Some(f(a, b))
  }

  def sequence[A](a: List[Option[A]]): Option[List[A]] = Some(a.map(_.getOrElse(None)))

  def traverse[A, B](a: List[A])(f: A => Option[B]): Option[List[B]] = Some(a.map(f(_).getOrElse(None)))

  def sequenceViaTraverse[A](a: List[Option[A]]): Option[List[A]] = traverse(a)(x => x)
}
