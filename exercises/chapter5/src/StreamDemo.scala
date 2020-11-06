import scala.util.Random

object StreamDemo {

  sealed trait Stream[+A] {

    def toList: List[A] = {
      // 因为不是尾调用，大list时会stack overflow
      //      case Empty => Nil
      //      case Cons(h, t) => h () :: t().toList
      // 这个就是尾调用
      //      @annotation.tailrec
      //      def go(s: Stream[A], acc: List[A]): List[A] = s match {
      //        case Cons(h,t) => go(t(), h() :: acc)
      //        case _ => acc
      //      }
      //      go(this, List()).reverse
      val buf = new collection.mutable.ListBuffer[A]

      @annotation.tailrec
      def go(s: Stream[A]): List[A] = s match {
        case Cons(h, t) => buf += h(); go(t())
        case _ => buf.toList
      }

      go(this)
    }

    def foldRight[B](z: => B)(f: (A, => B) => B): B = // The arrow `=>` in front of the argument type `B` means that the function `f` takes its second argument by name and may choose not to evaluate it.
      this match {
        case Cons(h, t) => f(h(), t().foldRight(z)(f)) // If `f` doesn't evaluate its second argument, the recursion never occurs.
        case _ => z
      }

    def exists(p: A => Boolean): Boolean =
    //      this match {
    //        case Cons(h, t) => p(h()) || t().exists(p)
    //        case _ => false
    //      }
      foldRight(false)((a, b) => p(a) || b)

    // Here `b` is the unevaluated recursive step that folds the tail of the stream.
    // If `p(a)` returns `true`, `b` will never be evaluated and the computation terminates early.
    // 虽然使用了foldRight，但是因为foldRight中第二个元素时非严格求值，所以只要p(a)为true，那么foldRight就不用递归
    // 也就不用继续求值了

    @annotation.tailrec
    final def find(f: A => Boolean): Option[A] = this match {
      case Empty => None
      case Cons(h, t) => if (f(h())) Some(h()) else t().find(f)
    }

    def take(n: Int): Stream[A] = this match {
      case Cons(h, t) if n > 1 => Stream.cons(h(), t().take(n - 1))
      case Cons(h, _) if n == 1 => Stream.cons(h(), Stream.empty)
      case _ => Stream.empty
    }

    def takeViaUnfold(n: Int): Stream[A] =
      Stream.unfold((this, n)) {
        case (Cons(h, t), 1) => Some(h(), (Stream.empty, 0))
        case (Cons(h, t), n) if n > 1 => Some(h(), (t(), n - 1))
        case _ => None
      }

    @annotation.tailrec
    final def drop(n: Int): Stream[A] = this match {
      case Cons(_, t) if n > 0 => t().drop(n - 1)
      case _ => this
    }

    def takeWhile(p: A => Boolean): Stream[A] = this match {
      case Cons(h, t) if p(h()) => Stream.cons(h(), t().takeWhile(p))
      case _ => Stream.empty
    }

    def takeWhileViaUnfold(p: A => Boolean): Stream[A] =
      Stream.unfold(this) {
        case Cons(h, t) if p(h()) => Some(h(), t())
        case _ => None
      }

    def takeWhileViaFoldRight(p: A => Boolean): Stream[A] =
      foldRight(Stream.empty[A])((h, t) =>
        if (p(h)) Stream.cons(h, t)
        else Stream.empty)

    def forAll(p: A => Boolean): Boolean =
      this match {
        case Cons(h, t) => p(h()) && t().forAll(p)
        case _ => true
      }

    //    {
    //      foldRight(true)((a, b) => p(a) && b)
    //    }

    def headOption: Option[A] = this match {
      case Empty => None
      // 必须对h thunk显式地调用h()强制求值
      case Cons(h, t) => Some(h())
    }

    def headOptionViaFoldRight: Option[A] =
      foldRight(None: Option[A])((a, _) => Some(a))

    // 5.7 map, filter, append, flatmap using foldRight. Part of the exercise is
    // writing your own function signatures.

    def map[B](f: A => B): Stream[B] =
      foldRight(Stream.empty[B])((a, b) => Stream.cons(f(a), b))

    def mapViaUnfold[B](f: A => B): Stream[B] =
      Stream.unfold(this) {
        case Cons(h, t) => Some(f(h()), t())
        case _ => None
      }

    def filter(f: A => Boolean): Stream[A] =
    // 不能直接返回this，那么只要有一个true就会返回这个stream本身，也就没有意义了
      foldRight(Stream.empty[A])((h, t) => if (f(h)) Stream.cons(h, t) else t)

    def append[B >: A](s: => Stream[B]): Stream[B] =
      foldRight(s)((h, t) => Stream.cons(h, t))

    def flatMap[B](f: A => Stream[B]): Stream[B] =
      foldRight(Stream.empty[B])((h, t) => f(h).append(t))


    def zipWith[B, C](s2: Stream[B])(f: (A, B) => C): Stream[C] =
      Stream.unfold((this, s2)) {
        case (Cons(h1, t1), Cons(h2, t2)) => Some(f(h1(), h2()), (t1(), t2()))
        case _ => None
      }

    // special case of `zipWith`
    def zip[B](s2: Stream[B]): Stream[(A, B)] =
      zipWith(s2)((_, _))

    def zipAll[B](s2: Stream[B]): Stream[(Option[A], Option[B])] =
      zipWithAll(s2)((_, _))

    def zipWithAll[B, C](s2: Stream[B])(f: (Option[A], Option[B]) => C): Stream[C] =
      Stream.unfold((this, s2)) {
        case (Empty, Empty) => None
        case (Cons(h, t), Empty) => Some(f(Some(h()), Option.empty[B]) -> (t(), Stream.empty[B]))
        case (Empty, Cons(h, t)) => Some(f(Option.empty[A], Some(h())) -> (Stream.empty[A] -> t()))
        case (Cons(h1, t1), Cons(h2, t2)) => Some(f(Some(h1()), Some(h2())) -> (t1() -> t2()))
      }

    // 要注意判断第一个stream是否empty！
    def startsWith[B](s: Stream[B]): Boolean =
      zipAll(s).takeWhile(!_._2.isEmpty) forAll {
        case (h,h2) => h == h2
      }

    def tails: Stream[Stream[A]] =
      Stream.unfold(this) {
        case Empty => None
        case s => Some((s, s drop 1))
      } append Stream(Stream.empty)

    // 组合所有tail的stream和startsWith
    def hasSequence[A](s: Stream[A]): Boolean = tails exists(_ startsWith s)

    def scanRight[B](z: B)(f: (A, => B) => B): Stream[B] =
      foldRight((z, Stream(z)))((a, p0) => {
        // p0 is passed by-name and used in by-name args in f and cons.
        // So use lazy val to ensure only one evaluation...
        lazy val p1 = p0
        val b2 = f(a, p1._1)
        (b2, Stream.cons(b2, p1._2))
      })._2
  }

  case object Empty extends Stream[Nothing]

  // 一个非空的stream由head和tail组成，都是非严格求值。这里必须明确强制求值的thunk
  case class Cons[+A](h: () => A, t: () => Stream[A]) extends Stream[A]

  object Stream {
    // 用于创建空stream的构造器
    // 这是一个智能构造器，所以写法上首字母小写
    def cons[A](hd: => A, tl: => Stream[A]): Stream[A] = {
      // 对惰性求值做缓存，避免重复求值
      lazy val head = hd
      lazy val tail = tl
      Cons(() => head, () => tail)
    }

    // 用于创建特定类型的智能构造器
    // 可以看作是类型接口
    def empty[A]: Stream[A] = Empty

    // 根据多个元素构建Stream
    def apply[A](as: A*): Stream[A] =
      if (as.isEmpty) empty
      else cons(as.head, apply(as.tail: _*))

    val ones: Stream[Int] = Stream.cons(1, ones)
    val onesViaUnfold: Stream[Int] = unfold(1)(_ => Some((1, 1)))

    def from(n: Int): Stream[Int] =
      cons(n, from(n + 1))

    def fromViaUnfold(n: Int): Stream[Int] = unfold(n)(n => Some((n, n + 1)))

    def constant[A](a: A): Stream[A] = {
      cons(a, constant(a))
    }

    def constantViaUnfold[A](a: A): Stream[A] = unfold(a)(a => Some((a, a)))

    val fibs = {
      def go(prepre: Int, pre: Int): Stream[Int] =
        cons(prepre, go(prepre, pre))

      go(0, 1)
    }

    val fibsViaUnfold = unfold((0, 1)){case (prepre, pre) => Some((prepre, (pre, pre + prepre)))}


    def unfold[A, S](z: S)(f: S => Option[(A, S)]): Stream[A] =
    // Option用于表示Stream合适结束
      f(z) match {
        case Some((h, s)) => cons(h, unfold(s)(f))
        case None => empty
      }


    def main(args: Array[String]): Unit = {

      val s = Stream(412, 512, 621, 35, 1263, 1235, 126, 15, 23, 152, 61, 23, 51231, 2345, 123, 6124, 1235, 12, 356)
      println(s.toList)
      println(s.headOption)
      println(s.headOptionViaFoldRight)
      println(s.toList)
      println(s.take(10).toList)
      println(s.drop(10).toList)
      println(s.takeWhile(_ >= 50).toList)
      println(s.takeWhileViaFoldRight(_ >= 50).toList)
      println(s.forAll(_ >= 0))
      println(s.map(_.toHexString).toList)
      println(s.filter(_ > 2000).toList)
      println(s.append(s).toList)
      println(s.flatMap(i => Stream.apply(i.toHexString)).toList)
    }

  }

}
