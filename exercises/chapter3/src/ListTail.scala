object ListTail {

  def tail[A](list : List[A]) : List[A] =
  list match {
      // 做成错误更好！
    case Nil => Nil
    // 不实装在list的伴生类中无法编译？
    case Cons(_, t) => List(t)
  }

  def setHead[A](h : A, list: List[A]):List[A]=
    h match {
      case None => list
      case _ => list match {
        case Nil => sys.error("empty list")
        case Cons(_, t) => Cons(h, t)
      }
    }

  def drop[A](n : Int, list : List[A]) : List[A] = n match {
    case 0 => list
    case x => list match {
    case Nil => Nil
    case Cons(_, t) => drop(n - 1, t)
    }
  }

  def dropWhile[A](list : List[A], f: A => Boolean) : List[A] = list match {
    case Cons(h, t) if (f(h)) => dropWhile(t, f)
    case _ => l
  }

  def main(args: Array[String]): Unit = {
    println(tail[String](List("aaa", "bbb", "ccc", "ddd")))
  }
}
