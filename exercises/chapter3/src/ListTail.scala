object ListTail {

  def tail[A](list : List[A]) : List[A] =
  list match {
      // 做成错误更好！
    case Nil => Nil
    // 不实装在list的伴生类中无法编译？
    case h :: t => t
  }

  def setHead[A](h : A, list: List[A]):List[A]=
    h match {
      case None => list
      case _ => list match {
        case Nil => sys.error("empty list")
        case head :: tail => h :: tail
      }
    }

  def drop[A](n : Int, list : List[A]) : List[A] = n match {
    case 0 => list
    case x => list match {
    case Nil => Nil
    case h :: t => drop(n - 1, t)
    }
  }

  def dropWhile[A](list : List[A], f: A => Boolean) : List[A] = list match {
    case h :: t if (f(h)) => dropWhile(t, f)
    case _ => list
  }

  // 不是尾调用，所以会占用stack
  def init[A](l : List[A]): List[A] = l match {
    case Nil => Nil
    case _ :: Nil => Nil
    case h :: t => h :: init(t)
  }

  // 所以只能通过可变的列表完成，这个递归就是尾递归了。
  // 因为这个可变也只是内部完成，所以RT（引用透明）还是保护住了
  // 最后还有一种就是以逆向顺序复制元素，最后再逆向
  def init2[A](l : List[A]): List[A] = {
    import collection.mutable.ListBuffer
    val buf = new ListBuffer[A]
    @annotation.tailrec
    def go(cur: List[A]): List[A] = cur match {
      case Nil => sys.error("init of empty list")
      case _ :: Nil => List(buf.toList: _*)
      case h :: t => buf += h; go(t)
    }
    go(l)
  }

  def main(args: Array[String]): Unit = {
    val l = List("aaa", "bbb", "ccc", "ddd")
    println(tail[String](l))
    println(setHead[String]("head", l))
    println(drop[String](2, l))
    println(dropWhile[String](l, s => s.size == 3))
    println(init[String](l))
    println(init2[String](l))
  }
}
