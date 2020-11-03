object IsSorted {

  // 尾调用。每一次去掉一个，比较两个
  @annotation.tailrec
  def isSortedRecur[A](as: Array[A], ordered: (A, A) => Boolean): Boolean =
  as.length match {
    case 0 => true
    case 1 => true
    case _ => {
    // println(as.mkString(", "))
      ordered(as(0), as(1)) && isSortedRecur(as.tail, ordered)
    }
  }

  // 遍历写法。可以考虑使用sliding或者zip?
  def isSorted[A](as: Array[A], ordered: (A, A) => Boolean): Boolean =
    as.length match {
      case 0 => true
      case x => {
        for (i <- (1 until x)){
          if (!ordered(as(i - 1), as(i))) return false
        }
        true
      }
    }

  def main(args: Array[String]): Unit = {
    val arrays = Array(Array("abb", "acc", "bcc", "bdd"), Array("abb", "dcc", "bcc", "bdd"))
    arrays.foreach(array => println(isSorted(array, (str1 : String, str2 : String) => str1.compareTo(str2) < 0)))
  }
}
